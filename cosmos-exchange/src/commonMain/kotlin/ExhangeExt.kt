package com.brd.exchange

val ExchangeModel.Mode.isTrade: Boolean
    get() = this == ExchangeModel.Mode.TRADE