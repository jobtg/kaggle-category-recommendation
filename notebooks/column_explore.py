"""
Determine fundamental column details.
"""
import logging


def series_details(series, series_name):
    """
    Analyses a series and returns relevant details for later feature creation and/or selection.
    :param series: Series
    :returns: Dictionary with details about the series.
    """
    series_property = {}
    
    if 'describe' in dir(series) and series.dtype not in ['object']:
        series_property = {
            'description': series.describe().compute(),
        }
    
    if 'id' in series_name:
        series_property['uniques'] = len(series.unique().values.compute())
    elif 'category' in series_name:
        series_property['uniques'] = series.unique().values.compute()
    elif series.dtype == 'object':
        series_property['top_values'] = series.values.compute()[0:50]
    
    return series_property
    
def dataframe_explore(dataframe, columns=[]):
    """
    Analyses Dask dataframe.
    :param dataframe: Dask dataframe
    :param columns: List of columns to analyse (default: all columns)
    :returns: Dictionary with details per column.
    """
    # Determine column properties, starting with the full dataframe.
    # This only contains the number of rows per column.
    logging.info('Analysing dataframe...')

    column_property = {
        'dataframe': {
            'count': dataframe.count().compute()
        }
    }
    
    columns_analyse = columns if columns else dataframe.columns
    
    for column in columns_analyse:
        if column in dataframe.columns:
            logging.info('Analysing column: %s...', column)

            column_property[column] = series_details(dataframe[column], column)
        else:
            logging.warn('Could not find column %s...', column)

    return column_property
