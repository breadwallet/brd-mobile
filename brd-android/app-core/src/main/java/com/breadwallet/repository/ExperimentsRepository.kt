/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 8/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.repository

import android.content.Context
import com.breadwallet.model.Experiment
import com.breadwallet.model.Experiments
import com.platform.network.ExperimentsClientImpl

interface ExperimentsRepository {

    val experiments: Map<String, Experiment>

    /**
     * Refresh the set of experiments.
     */
    fun refreshExperiments(context: Context)

    /**
     * Check if a experiment is available or not.
     */
    fun isExperimentActive(experiment: Experiments): Boolean

}

/**
 * Implementation that stores the experiments in memory.
 */
object ExperimentsRepositoryImpl : ExperimentsRepository {

    private var experimentsCache = mapOf<String, Experiment>()
    override val experiments get() = experimentsCache

    override fun refreshExperiments(context: Context) {
        experimentsCache = ExperimentsClientImpl.getExperiments(context).map { it.name to it }.toMap()
    }

    override fun isExperimentActive(experiment: Experiments) = experiments[experiment.key]?.active ?: false
}
