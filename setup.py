from setuptools import setup, find_packages

config = {
    'description': 'Elo Category Recommender',
    'author': 'Team BaCo',
    'install_requires': [
        'pylint',
        'tensorflow',
        'apache-beam',
        'google',
    ],
    'scripts': [],
    'name': 'elo-category-recommendation',
    'version': '0.0.1',
    'packages': find_packages(),
    'include_package_data': True,
}

setup(**config)
