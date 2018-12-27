from __future__ import print_function

import argparse
import os
import json

import tensorflow as tf
from confload import Config

import model

Config.load('./config.yml')


def _get_session_config():
    """
    Determines tf.ConfigProto from environment variables.
    :return: Tensorflow configuration (tf.ConfigProto).
    """
    tf_config = json.loads(os.environ.get('TF_CONFIG', '{}'))

    try:
        if tf_config['task']['type'] == 'master':
            # The master communicates with itself
            # and the ps (parameter server).
            return tf.ConfigProto(
                device_filters=['/job:ps', '/job:master']
            )
        elif tf_config['task']['type'] == 'worker':
            return tf.ConfigProto(
                device_filters=['/job:ps', '/job:worker/task:%d' % tf_config['task']['index']]
            )
    except KeyError:
        return None


def train_and_evaluate(args):
    """
    Run training and evaluate.
    :param args: Arguments for model
    :return: train_and_evaluate
    """
    train_input = lambda: model.input_fn(
        args.base_directory + 'train',
        num_epochs=args.num_epochs,
        batch_size=args.train_batch_size
    )

    eval_input = lambda: model.input_fn(
        args.base_directory + 'eval'
    )

    train_spec = tf.estimator.TrainSpec(train_input, max_steps=args.train_steps)

    exporter = tf.estimator.FinalExporter('census', model.json_serving_input_fn)

    eval_spec = tf.estimator.EvalSpec(
        eval_input,
        steps=args.eval_steps,
        exporters=[exporter],
        name='recommender-eval'
    )

    run_config = tf \
        .estimator \
        .RunConfig(session_config=_get_session_config()) \
        .replace(model_dir=args.job_dir)

    estimator = model.build_estimator(
        config=run_config,
        embedding_size=args.embedding_size,
        hidden_units=[
            max(2, int(args.first_layer_size * args.scale_factor ** i))
            for i in range(args.num_layers)
        ]
    )

    tf.estimator.train_and_evaluate(estimator, train_spec, eval_spec)


if __name__ == '__main__':
    config = Config.get('model')

    parser = argparse.ArgumentParser()

    # Input Arguments
    parser.add_argument(
        '--base_directory',
        help='GCS file or local paths to training data',
        nargs='+',
        default=config['base_path'] + 'processed/'
    )

    parser.add_argument(
        '--job-dir',
        help='GCS location to write checkpoints and export models',
        default='/tmp/elo-recommender'
    )

    parser.add_argument(
        '--num-epochs',
        help="""\
        Maximum number of training data epochs on which to train.
        If both --max-steps and --num-epochs are specified,
        the training job will run for --max-steps or --num-epochs,
        whichever occurs first. If unspecified will run for --max-steps.\
        """,
        type=int
    )

    parser.add_argument(
        '--train-batch-size',
        help='Batch size for training steps',
        type=int,
        default=40
    )

    parser.add_argument(
        '--eval-batch-size',
        help='Batch size for evaluation steps',
        type=int,
        default=40
    )

    parser.add_argument(
        '--embedding-size',
        help='Number of embedding dimensions for categorical columns',
        default=8,
        type=int
    )

    parser.add_argument(
        '--first-layer-size',
        help='Number of nodes in the first layer of the DNN',
        default=100,
        type=int
    )

    parser.add_argument(
        '--num-layers',
        help='Number of layers in the DNN',
        default=4,
        type=int
    )

    parser.add_argument(
        '--scale-factor',
        help='How quickly should the size of the layers in the DNN decay',
        default=0.7,
        type=float
    )

    parser.add_argument(
        '--train-steps',
        help="""\
        Steps to run the training job for. If --num-epochs is not specified,
        this must be. Otherwise the training job will run indefinitely.""",
        default=100,
        type=int
    )

    parser.add_argument(
        '--eval-steps',
        help='Number of steps to run evalution for at each checkpoint',
        default=100,
        type=int
    )

    parser.add_argument(
        '--export-format',
        help='The input format of the exported SavedModel binary',
        choices=['JSON'],  # , 'CSV', 'EXAMPLE'],
        default='JSON'
    )

    parser.add_argument(
        '--verbosity',
        choices=['DEBUG', 'ERROR', 'FATAL', 'INFO', 'WARN'],
        default='DEBUG'
    )

    train_and_evaluate(args)
