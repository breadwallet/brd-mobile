import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class BlocScreen<B extends Cubit<S>, S> extends StatelessWidget {
  const BlocScreen({
    required this.bloc,
    this.builderCondition,
    required this.builder,
    this.listenerCondition,
    required this.listener,
  });

  final B bloc;
  final BlocBuilderCondition<S>? builderCondition;
  final BlocWidgetBuilder<S> builder;

  final BlocListenerCondition<S>? listenerCondition;
  final BlocWidgetListener<S> listener;

  @override
  Widget build(BuildContext context) {
    return BlocListener<B, S>(
      listenWhen: listenerCondition,
      listener: listener,
      bloc: bloc,
      child: BlocBuilder<B, S>(
        bloc: bloc,
        buildWhen: builderCondition,
        builder: builder,
      ),
    );
  }
}
