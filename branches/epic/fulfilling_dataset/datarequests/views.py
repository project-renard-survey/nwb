from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect
from django.shortcuts import render_to_response, get_object_or_404
from django.template import RequestContext
from django.template.defaultfilters import slugify

from epic.comments.forms import PostCommentForm
from epic.core.models import Item
from epic.datarequests.forms import DataRequestForm
from epic.datarequests.models import DataRequest
from epic.tags.models import Tagging

def view_datarequests(request):
    datarequests = DataRequest.objects.active().exclude(status='C').order_by('-created_at')
    datarequest_form = DataRequestForm()
    return render_to_response('datarequests/view_datarequests.html', 
                              {'datarequests': datarequests,
                               'datarequest_form':datarequest_form,}, 
                              context_instance=RequestContext(request))

def view_datarequest(request, item_id, slug):
    datarequest = get_object_or_404(DataRequest, pk=item_id)
    form = PostCommentForm()
    user = request.user
    
    return render_to_response('datarequests/view_datarequest.html', 
                              {'datarequest': datarequest, 'form': form},
                               context_instance=RequestContext(request))

@login_required
def new_datarequest(request):
    user = request.user
    if request.method != 'POST':
        form = DataRequestForm()
    else:
        form = DataRequestForm(request.POST)
        if form.is_valid():
            datarequest = form.save(commit=False)
            datarequest.creator = user
            datarequest.slug = slugify(datarequest.name)
            datarequest.is_active = True
            datarequest.save()
            
            tag_names = form.cleaned_data["tags"]
            Tagging.objects.update_tags(tag_names=tag_names, 
                                        item=datarequest, 
                                        user=user)
                
            return HttpResponseRedirect(reverse('epic.datarequests.views.view_datarequest', 
                                                kwargs={'item_id':datarequest.id, 
                                                        'slug':datarequest.slug}))
        else:
            pass
    return render_to_response('datarequests/new_datarequest.html', 
                              {'form':form}, 
                              context_instance=RequestContext(request))

@login_required
def edit_datarequest(request, item_id, slug):
    user = request.user
    datarequest = get_object_or_404(DataRequest,pk=item_id)
    if datarequest.creator != user:
        #only a request's owner should be able to change it
        return HttpResponseRedirect(
                reverse('epic.datarequests.views.view_datarequest', 
                        kwargs={'item_id':datarequest.id, 
                                'slug':datarequest.slug}))
    else:
        if request.method != 'POST':
            #User hasn't seen the form yet, so fill in what we know
            current_tags = Tagging.objects.get_edit_string(item=datarequest, 
                                                           user=user)
            form = DataRequestForm(instance=datarequest, 
                                   initial={'tags': current_tags})
        else:
            form = DataRequestForm(request.POST, instance=datarequest)
            if form.is_valid():
                tag_names = form.cleaned_data["tags"]
                Tagging.objects.update_tags(tag_names=tag_names, 
                                        item=datarequest, 
                                        user=user)
            
                datarequest = form.save(commit=False)
                datarequest.slug = slugify(datarequest.name)  
                datarequest.save()
                
                return HttpResponseRedirect(
                        reverse('epic.datarequests.views.view_datarequest', 
                                kwargs={'item_id':datarequest.id, 
                                        'slug':datarequest.slug}))
            else:
                #Form will have errors which will be displayed by the 
                #  new_datarequest.html page
                pass
        return render_to_response('datarequests/edit_datarequest.html', 
                                  {'form': form, 'datarequest': datarequest}, 
                                  context_instance=RequestContext(request))
    
@login_required
def cancel_datarequest(request, item_id, slug):
    user = request.user
    datarequest = get_object_or_404(DataRequest,pk=item_id)
    if datarequest.creator != user:
        #only a request's owner should be able to change it
        return HttpResponseRedirect(reverse('epic.datarequests.views.view_datarequest', 
                                            kwargs={'item_id':datarequest.id, 
                                                    'slug':datarequest.slug}))
    else:
        datarequest.cancel()
        datarequest.save()
        return HttpResponseRedirect(reverse('epic.datarequests.views.view_datarequest', 
                                            kwargs={'item_id':datarequest.id, 
                                                    'slug':datarequest.slug}))

@login_required
def fulfill_datarequest(request, item_id, slug, fulfilling_item_id=None):
    user = request.user
    datarequest = get_object_or_404(DataRequest,pk=item_id)
    
    if fulfilling_item_id and datarequest.creator == user:
        fulfilling_item = get_object_or_404(Item, pk=fulfilling_item_id)
        datarequest.fulfilling_item = fulfilling_item
        datarequest.save()
        
    if datarequest.creator != user:
        #only a request's owner should be able to change it
        return HttpResponseRedirect(reverse('epic.datarequests.views.view_datarequest', 
                                            kwargs={'item_id':datarequest.id, 
                                                    'slug':datarequest.slug}))
    else:
        datarequest.fulfill()
        datarequest.save()
        return HttpResponseRedirect(reverse('epic.datarequests.views.view_datarequest', 
                                            kwargs={'item_id':datarequest.id, 
                                                    'slug':datarequest.slug}))

@login_required
def choose_fulfilling_item(request, fulfilling_item_id):
    user = request.user
    item = get_object_or_404(Item, pk=fulfilling_item_id)
    requests = DataRequest.objects.active().filter(status='U', creator=user).order_by('-created_at')
    return render_to_response('datarequests/choose_fulfilling_item.html',
                              {'item':item,
                               'requests':requests},
                              context_instance=RequestContext(request))