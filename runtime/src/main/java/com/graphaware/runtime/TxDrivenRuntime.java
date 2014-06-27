/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.Set;

/**
 * {@link BaseGraphAwareRuntime} that registers itself as a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler},
 * translates {@link org.neo4j.graphdb.event.TransactionData} into {@link com.graphaware.tx.event.improved.api.ImprovedTransactionData}
 * and lets registered {@link com.graphaware.runtime.module.TxDrivenModule}s deal with the data before each transaction
 * commits, in the order the modules were registered.
 *
 * @param <T> implementation of {@link com.graphaware.runtime.module.TxDrivenModule} that this runtime supports.
 */
public abstract class TxDrivenRuntime<T extends TxDrivenModule> extends BaseGraphAwareRuntime implements TransactionEventHandler<Void> {

    private final TxDrivenModuleManager<T> txDrivenModuleManager;

    /**
     * Create a new instance of the runtime with {@link com.graphaware.runtime.config.DefaultRuntimeConfiguration}.
     */
    protected TxDrivenRuntime(TxDrivenModuleManager<T> txDrivenModuleManager) {
        this(DefaultRuntimeConfiguration.getInstance(), txDrivenModuleManager);
    }

    protected TxDrivenRuntime(RuntimeConfiguration configuration, TxDrivenModuleManager<T> txDrivenModuleManager) {
        super(configuration);
        this.txDrivenModuleManager = txDrivenModuleManager;
    }


    protected abstract Class<T> supportedModule();

    protected void doRegisterModule(RuntimeModule module) {
        if (supportedModule().isAssignableFrom(module.getClass())) {
            txDrivenModuleManager.registerModule((T) module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void beforeCommit(TransactionData data) throws Exception {
        if (!makeSureIsStarted()) {
            return null;
        }

        txDrivenModuleManager.throwExceptionIfIllegal(data);

        txDrivenModuleManager.beforeCommit(new LazyTransactionData(data));

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterCommit(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterRollback(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<String> initializeModules() {
        return txDrivenModuleManager.initializeModules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCleanup(Set<String> usedModules) {
        txDrivenModuleManager.removeUnusedModules(usedModules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdownModules() {
        txDrivenModuleManager.shutdownModules();
    }
}
