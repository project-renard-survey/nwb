from datasets.models import DataSet

#######################
# Create the DataSets #
#######################

def _create_datasets():
	first_data_set = DataSet.objects.create(creator=bob, name='dataset1', description='this is the first dataset', slug='dataset1')
	second_data_set = DataSet.objects.create(creator=admin, name='dataset2', description='this is the second dataset', slug='dataset2')
	
######################################
# Generate the actual fixtures here. #
######################################

_create_datasets()