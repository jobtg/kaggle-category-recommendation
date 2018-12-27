"""
This model is based on
https://github.com/GoogleCloudPlatform/cloudml-samples/blob/master/census/estimator/trainer/model.py
"""

from __future__ import print_function

import tensorflow as tf

# Set target column.
LABEL_COLUMN = 'target'
LABELS = []

CSV_COLUMNS = []

CSV_COLUMN_DEFAULTS = [0.0] * len(CSV_COLUMNS)

# Determine input columns for Tensorflow model.
INPUT_COLUMNS = [
    # List of input columns
    # tf.feature_column.categorical_column_with_vocabulary_list()
    # tf.feature_column.categorical_column_with_vocabulary_list()
    # tf.feature_column.crossed_column()
    # tf.feature_column.numeric_column()
    # tf.feature_column.categorical_column_with_hash_bucket()
    # ...
]

# Columns which are not used.
UNUSED_COLUMNS = set(CSV_COLUMNS) - {col.name for col in INPUT_COLUMNS} - {LABEL_COLUMN}


def build_estimator(config, embedding_size=8, hidden_units=None):
    """

    :param config:
    :param embedding_size:
    :param hidden_units:
    :return:
    """
    return tf.estimator.DNNLinearCombinedRegressor(
        config=config,
        linear_feature_columns=[],
        dnn_feature_columns=[],
        dnn_hidden_units=hidden_units or [100, 75, 50, 25]
    )


def _decode_csv(line):
    """Takes the string input tensor and returns a dict of rank-2 tensors."""

    # Takes a rank-1 tensor and converts it into rank-2 tensor
    # Example if the data is ['csv,line,1', 'csv,line,2', ..] to
    # [['csv,line,1'], ['csv,line,2']] which after parsing will result in a
    # tuple of tensors: [['csv'], ['csv']], [['line'], ['line']], [[1], [2]]
    row_columns = tf.expand_dims(line, -1)
    columns = tf.decode_csv(row_columns, record_defaults=CSV_COLUMN_DEFAULTS)
    features = dict(zip(CSV_COLUMNS, columns))

    # Remove unused columns
    for col in UNUSED_COLUMNS:
        features.pop(col)
    return features


def json_serving_input_fn():
    """
    Build the serving inputs.
    :return: TF.ServingInputReceiver.
    """
    inputs = {
        feature.name: tf.placeholder(shape=[None], dtype=feature.dtype)
        for feature in INPUT_COLUMNS
    }

    return tf.estimator.export.ServingInputReceiver(inputs, inputs)


def input_fn(filenames,
             num_epochs=None,
             shuffle=True,
             skip_header_lines=0,
             batch_size=200):
    dataset = tf.data.TextLineDataset(filenames).skip(skip_header_lines).map(
        _decode_csv
    )

    if shuffle:
        dataset = dataset.shuffle(buffer_size=batch_size * 10)

    iterator = dataset \
        .repeat(num_epochs) \
        .batch(batch_size) \
        .make_one_shot_iterator()

    features = iterator.get_next()

    return features, features.pop(LABEL_COLUMN)
