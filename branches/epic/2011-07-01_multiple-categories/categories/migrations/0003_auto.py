# encoding: utf-8
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models

class Migration(SchemaMigration):
    
    def forwards(self, orm):
        
        # Removing index on 'Category', fields ['name']
        db.delete_index('categories_category', ['name'])
    
    
    def backwards(self, orm):
        
        # Adding index on 'Category', fields ['name']
        db.create_index('categories_category', ['name'])
    
    
    models = {
        'categories.category': {
            'Meta': {'unique_together': "(('name', 'description'),)", 'object_name': 'Category'},
            'created_at': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'db_index': 'True', 'blank': 'True'}),
            'description': ('django.db.models.fields.TextField', [], {'null': 'True', 'blank': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '256'})
        }
    }
    
    complete_apps = ['categories']
