from epic.core.models import Item
from epic.datasets.forms import NewDataSetForm, RatingDataSetForm, TagDataSetForm
from epic.datasets.models import DataSetFile, DataSet, RATING_SCALE

from django.contrib.auth.models import User
from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect
from django.shortcuts import render_to_response, get_object_or_404

from django.contrib.auth.decorators import login_required

from epic.comments.models import Comment
from epic.comments.forms import PostCommentForm
from epic.comments.views import make_comment_view

from datetime import datetime


def index(request):
    datasets = DataSet.objects.all().order_by('-created_at')
    return render_to_response('datasets/index.html', {'datasets': datasets, 'user': request.user})

def view_dataset(request, dataset_id=None):
	dataset = get_object_or_404(DataSet, pk=dataset_id)
	post_comment_form = PostCommentForm()
	user = request.user
	
	return render_to_response('datasets/view_dataset.html', {
		'dataset': dataset,
		'user': user,
		'post_comment_form': post_comment_form
	})

post_dataset_comment = make_comment_view(
	DataSet,
	"epic.datasets.views.view_dataset",
	"datasets/view_dataset.html",
	"dataset")
#@login_required
#def post_dataset_comment(request, dataset_id):
#	user = request.user
#	
#	if user.is_authenticated():
#		dataset = get_object_or_404(DataSet, pk=dataset_id)
#		
#		if request.method != "POST":
#			return HttpResponseRedirect(reverse("epic.datasets.views.view_dataset", args=(dataset_id)))
#		else:
#			post_comment_form = PostCommentForm(request.POST)
#			
#			if post_comment_form.is_valid():
#				comment_value = post_comment_form.cleaned_data["comment"]
#				comment = Comment(posting_user=user, parent_item=dataset, value=comment_value)
#				comment.save()
#				
#				return HttpResponseRedirect(reverse("epic.datasets.views.view_dataset", args=(dataset_id)))
#			else:
#				return render_to_response("datasets/view_dataset.html", {
#					"dataset": dataset,
#					"user": user,
#					"post_comment_form": post_comment_form
#				})
#	
#	return HttpResponseRedirect("/login/?next=%s" % request.path)

@login_required
def create_dataset(request):
    if request.method != 'POST':
        #user has not filled out the upload form yet
        
        #show them the upload form
        form = NewDataSetForm()
        return render_to_response('datasets/create_dataset.html', {'form':form, 'user':request.user,})
    else:
    	#user has filled out the upload form
        #handle the submission of their upload form  
        form = NewDataSetForm(request.POST, request.FILES)
        
        if form.is_valid():
        	name = form.cleaned_data['name']
        	description = form.cleaned_data['description']
        	uploaded_file = form.cleaned_data['file']
        	tags = form.cleaned_data['tags']
        	
        	#create a dataset with no files (yet), with metadata provided by the user
        	new_dataset = DataSet(creator=request.user, name=name, description=description)
        	new_dataset.save()
        	new_dataset.tags.update_tags(tags, user=request.user)
        	
        	#create file model objects for each file the user uploaded, and set their parent to be the new dataset
        	#TODO: add support for multiple files
        	new_datasetfile = DataSetFile(parent_dataset=new_dataset, file_contents=uploaded_file)
        	#(in addition to saving the uploaded file on the file system, with the save=True option this saves the model as well)
        	#TODO: put this in a directory specific to this dataset
        	new_datasetfile.file_contents.save(uploaded_file.name, uploaded_file, save=True)
        	#f.save() #this is redundant because of the save=True
        	
        	#show them the page for the dataset we just created
        	return HttpResponseRedirect(reverse('epic.datasets.views.view_dataset', kwargs={'dataset_id':new_dataset.id,}))
        else:
        	#form wasn't filled out correctly
            #show them the form again (modified to show which fields weren't filled out correctly)
            return render_to_response('datasets/create_dataset.html', {'form':form, 'user':request.user,})
              
    
   	
@login_required
def rate_dataset(request, dataset_id, input_rating=None):
	#TODO: Make commenting ajaxified?
	if input_rating:
		dataset = DataSet.objects.get(pk = dataset_id)
		user = request.user
		ip_address = request.META['REMOTE_ADDR']
		#Rely on the rating.add to make sure the that rating is valid
		rating = int(input_rating) 
		dataset.rating.add(rating, user, ip_address)
		dataset.save()
		return HttpResponseRedirect(reverse('epic.datasets.views.view_dataset', kwargs={'dataset_id':dataset.id,}))
	else:
		if request.method != 'POST':
			#show them the rate form
	  		form = RatingDataSetForm()
		else:
			# handle the submission of their rate form
			form = RatingDataSetForm(request.POST)
			if form.is_valid():
				rating = int(form.cleaned_data['rating']) #TODO: why doesn't it just return an int?
				ip_address = request.META['REMOTE_ADDR']
				user = request.user
				
				dataset = DataSet.objects.get(pk = dataset_id)
				dataset.rating.add(rating, user, ip_address)
				dataset.save()
				
				return HttpResponseRedirect(reverse('epic.datasets.views.view_dataset', kwargs={'dataset_id':dataset.id,}))
			else:
				print request.POST
				print form.errors
				
		return render_to_response('datasets/rate_dataset.html', {'form':form, 'user':request.user,})

@login_required
def tag_dataset(request, dataset_id):
	dataset = DataSet.objects.get(pk = dataset_id)
	if request.method != 'POST':
		current_tags = dataset.tags.get_edit_string(user=request.user)
		form = TagDataSetForm(initial={'tags': current_tags})
		return render_to_response('datasets/tag_dataset.html', {'form':form, 'user':request.user,})
	else:
		form = TagDataSetForm(request.POST)
		print request.POST
		if form.is_valid():
			tags = form.cleaned_data['tags']
			dataset.tags.update_tags(tags, user=request.user)
			return HttpResponseRedirect(reverse('epic.datasets.views.view_dataset', kwargs={'dataset_id':dataset.id,}))
		else:
			return render_to_response('datasets/tag_dataset.html', {'form':form, 'user':request.user,})