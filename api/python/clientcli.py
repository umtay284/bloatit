from elveos.model.member import Member
from elveos.model.feature import Feature
import elveos.core.server

elveos.core.server.server.set_host('http://127.0.0.1')


for feature in Feature.objects:
    print '============================================'
    print 'Feature : %s (#%s)' %(feature.title, feature.id)
    print '    Author : %s (#%s)' % (feature.author.name , feature.author.id) 
    print '    Description :'
    print feature.description
     
    

#print 'Members:'
#for member in Member.objects:
#    print member.id
