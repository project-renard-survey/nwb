from datarequests.models import DataRequest

######################################
# Create the Canceled DataRequests.  #
######################################

def _create_canceled_datarequests():
	first_canceled_data_request = DataRequest.objects.create(creator=bob, name='drC1', description='The first canceled datarequest', status='C')
	second_canceled_data_request = DataRequest.objects.create(creator=admin, name='drC2', description='The second canceled datarequest', status='C')
	third_canceled_data_request = DataRequest.objects.create(creator=bob, name='drC3', description='The third canceled datarequest', status='C')
	fourth_canceled_data_request = DataRequest.objects.create(creator=admin, name='drC4', description='The fourth canceled datarequest', status='C')
	fifth_canceled_data_request = DataRequest.objects.create(creator=bob, name='drC5', description='The fifth canceled datarequest', status='C')
	return

######################################
# Create the Fulfilled DataRequests. #
######################################

def _create_fulfilled_datarequests():
	first_fulfilled_data_request = DataRequest.objects.create(creator=bob, name='drF1', description='The first fulfilled datarequest', status='F')
	second_fulfilled_data_request = DataRequest.objects.create(creator=admin, name='drF2', description='The second fulfilled datarequest', status='F')
	third_fulfilled_data_request = DataRequest.objects.create(creator=bob, name='drF3', description='The third fulfilled datarequest', status='F')
	fourth_fulfilled_data_request = DataRequest.objects.create(creator=admin, name='drF4', description='The fourth fulfilled datarequest', status='F')
	fifth_fulfilled_data_request = DataRequest.objects.create(creator=bob, name='drF5', description='The fifth fulfilled datarequest', status='F')
	sixth_fulfilled_data_request = DataRequest.objects.create(creator=admin, name='drF6', description='The sixth fulfilled datarequest', status='F')
	return 

########################################
# Create the unfulfilled DataRequests. #
########################################

def _create_unfulfilled_datarequests():
	first_unfulfilled_data_request = DataRequest.objects.create(creator=bob, name='drU1', description='The first unfulfilled datarequest', status='U')
	second_unfulfilled_data_request = DataRequest.objects.create(creator=admin, name='drU2', description='The second unfulfilled datarequest', status='U')
	third_unfulfilled_data_request = DataRequest.objects.create(creator=bob, name='drU3', description='The third unfulfilled datarequest', status='U')
	fourth_unfulfilled_data_request = DataRequest.objects.create(creator=admin, name='drU4', description='The fourth unfulfilled datarequest', status='U')
	return

######################################
# Generate the actual fixtures here. #
######################################

_create_canceled_datarequests()
_create_fulfilled_datarequests()
_create_unfulfilled_datarequests()