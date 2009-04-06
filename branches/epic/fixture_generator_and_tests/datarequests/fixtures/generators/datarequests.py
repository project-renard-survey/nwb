from datarequests.models import DataRequest

######################################
# Create the Canceled DataRequests.  #
######################################

def _create_c_datarequests():
	drC1 = DataRequest.objects.create(creator=bob, name='drC1', description='The first canceled datarequest', status='C')
	drC2 = DataRequest.objects.create(creator=admin, name='drC2', description='The second canceled datarequest', status='C')
	drC3 = DataRequest.objects.create(creator=bob, name='drC3', description='The third canceled datarequest', status='C')
	drC4 = DataRequest.objects.create(creator=admin, name='drC4', description='The fourth canceled datarequest', status='C')
	drC5 = DataRequest.objects.create(creator=bob, name='drC5', description='The fifth canceled datarequest', status='C')
	return

######################################
# Create the Fulfilled DataRequests. #
######################################

def _create_f_datarequests():
	drF1 = DataRequest.objects.create(creator=bob, name='drF1', description='The first fulfilled datarequest', status='F')
	drF2 = DataRequest.objects.create(creator=admin, name='drF2', description='The second fulfilled datarequest', status='F')
	drF3 = DataRequest.objects.create(creator=bob, name='drF3', description='The third fulfilled datarequest', status='F')
	drF4 = DataRequest.objects.create(creator=admin, name='drF4', description='The fourth fulfilled datarequest', status='F')
	drF5 = DataRequest.objects.create(creator=bob, name='drF5', description='The fifth fulfilled datarequest', status='F')
	drF6 = DataRequest.objects.create(creator=admin, name='drF6', description='The sixth fulfilled datarequest', status='F')
	return 

########################################
# Create the unfulfilled DataRequests. #
########################################

def _create_u_datarequests():
	drU1 = DataRequest.objects.create(creator=bob, name='drU1', description='The first unfulfilled datarequest', status='U')
	drU2 = DataRequest.objects.create(creator=admin, name='drU2', description='The second unfulfilled datarequest', status='U')
	drU3 = DataRequest.objects.create(creator=bob, name='drU3', description='The third unfulfilled datarequest', status='U')
	drU4 = DataRequest.objects.create(creator=admin, name='drU4', description='The fourth unfulfilled datarequest', status='U')
	return

######################################
# Generate the actual fixtures here. #
######################################

_create_c_datarequests()
_create_f_datarequests()
_create_u_datarequests()