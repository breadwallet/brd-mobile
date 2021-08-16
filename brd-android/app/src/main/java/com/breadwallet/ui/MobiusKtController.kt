/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui

import android.os.Bundle
import android.view.View
import com.breadwallet.mobius.ConsumerDelegateKt
import com.breadwallet.mobius.QueuedConsumer
import com.breadwallet.mobius.QueuedConsumerKt
import com.breadwallet.ui.navigation.Navigator
import com.breadwallet.util.errorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kt.mobius.*
import kt.mobius.android.AndroidLogger
import kt.mobius.android.MobiusAndroid
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.runners.WorkRunners
import org.kodein.di.Kodein
import org.kodein.di.erased.instance
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("TooManyFunctions")
abstract class MobiusKtController<M, E, F>(
    args: Bundle? = null
) : BaseController(args),
    EventSource<E> {

    override val kodein by Kodein.lazy {
        extend(super.kodein)
    }

    protected val uiBindScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + errorHandler("uiBindScope")
    )

    private val navigator by instance<Navigator>()

    /** The default model used to construct [loopController]. */
    abstract val defaultModel: M

    /** The update function used to construct [loopFactory]. */
    abstract val update: Update<M, E, F>

    /** The init function used to construct [loopFactory]. */
    open val init: Init<M, F> = Init { First.first(it) }

    /** The effect handler used to construct [loopFactory]. */
    abstract val effectHandler: Connectable<F, E>

    private val isLoopActive = AtomicBoolean(false)
    private val logger = AndroidLogger.tag<M, E, F>(this::class.java.simpleName)
    private val loopFactory by lazy {
        Mobius.loop(update, effectHandler)
            .init(init)
            .eventRunner { WorkRunners.cachedThreadPool() }
            .effectRunner { WorkRunners.cachedThreadPool() }
            .logger(logger)
            .eventSource(this)
    }

    private val loopController by lazy {
        MobiusAndroid.controller(loopFactory, defaultModel)
    }

    private val eventConsumerDelegate = ConsumerDelegateKt<E>(QueuedConsumerKt())
    private val modelChannel = BroadcastChannel<M>(Channel.CONFLATED)

    /**
     * An entrypoint for adding platform events into a [MobiusLoop].
     *
     * When the loop has not been started, a [QueuedConsumer] is used
     * and all events will be dispatched when the loop is started.
     * Events dispatched from [onActivityResult] are one example of
     * of this use-case.
     */
    val eventConsumer: Consumer<E> = eventConsumerDelegate

    /** The currently rendered model. */
    val currentModel: M
        get() = loopController.model ?: defaultModel

    /** The previously rendered model or null  */
    var previousModel: M? = null
        private set

    /** Called when [view] can attach listeners to dispatch events via [output]. */
    open fun bindView(output: Consumer<E>): Disposable = Disposable { }

    /** Called when the model is updated or additional rendering is required. */
    open fun M.render() = Unit

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        loopController.connect(connectView())
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        previousModel = null
        loopController.start()
        currentModel.render()
    }

    override fun onDetach(view: View) {
        loopController.stop()
        super.onDetach(view)
    }

    override fun onDestroyView(view: View) {
        loopController.disconnect()
        super.onDestroyView(view)
    }

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        (eventConsumerDelegate.consumer as? QueuedConsumerKt)?.dequeueAll(eventConsumer)
        eventConsumerDelegate.consumer = eventConsumer
        return Disposable {
            eventConsumerDelegate.consumer = QueuedConsumerKt()
        }
    }

    private fun connectView(): Connectable<M, E> {
        return Connectable { consumer ->
            val connection = bindView(consumer)
            object : Connection<M> {
                override fun accept(value: M) {
                    value.render()
                    previousModel = value
                }

                override fun dispose() {
                    connection.dispose()
                }
            }
        }
    }

    inline fun <T, reified M2 : M> extractOrUnit(
        model: M?,
        crossinline extract: (M2) -> T
    ): Any? {
        return if (model is M2) model.run(extract) else Unit
    }

    /**
     * Invokes [block] only when the result of [extract] on
     * [this] is not equal to [extract] on [previousModel].
     *
     * [block] supplies the value extracted from [currentModel].
     */
    inline fun <T, reified M2 : M> M2.ifChanged(
        crossinline extract: (M2) -> T,
        crossinline block: (@ParameterName("value") T) -> Unit
    ) {
        val currentValue = extract(this)
        val previousValue = extractOrUnit(previousModel, extract)
        if (currentValue != previousValue) {
            block(currentValue)
        }
    }

    /**
     * Invokes [block] if the result of any extract functions on
     * [this] are not equal to the same function on the [previousModel].
     */
    inline fun <T1, T2, reified M2 : M> M2.ifChanged(
        crossinline extract1: (M2) -> T1,
        crossinline extract2: (M2) -> T2,
        crossinline block: () -> Unit
    ) {
        if (
            extract1(this) != extractOrUnit(previousModel, extract1) ||
            extract2(this) != extractOrUnit(previousModel, extract2)
        ) {
            block()
        }
    }

    /**
     * Invokes [block] if the result of any extract functions on
     * [this] are not equal to the same function on the [previousModel].
     */
    inline fun <T1, T2, T3, reified M2 : M> M2.ifChanged(
        crossinline extract1: (M2) -> T1,
        crossinline extract2: (M2) -> T2,
        crossinline extract3: (M2) -> T3,
        crossinline block: () -> Unit
    ) {
        if (
            extract1(this) != extractOrUnit(previousModel, extract1) ||
            extract2(this) != extractOrUnit(previousModel, extract2) ||
            extract3(this) != extractOrUnit(previousModel, extract3)
        ) {
            block()
        }
    }

    /**
     * Invokes [block] if the result of any extract functions on
     * [this] are not equal to the same function on the [previousModel].
     */
    @Suppress("ComplexCondition")
    inline fun <T1, T2, T3, T4, reified M2 : M> M2.ifChanged(
        crossinline extract1: (M2) -> T1,
        crossinline extract2: (M2) -> T2,
        crossinline extract3: (M2) -> T3,
        crossinline extract4: (M2) -> T4,
        crossinline block: () -> Unit
    ) {
        if (
            extract1(this) != extractOrUnit(previousModel, extract1) ||
            extract2(this) != extractOrUnit(previousModel, extract2) ||
            extract3(this) != extractOrUnit(previousModel, extract3) ||
            extract4(this) != extractOrUnit(previousModel, extract4)
        ) {
            block()
        }
    }
}
