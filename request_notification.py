from firebase_admin import messaging,db,_apps,initialize_app,auth
from random import choices
from string import digits

if (not len(_apps)):
    default_app = initialize_app()
db_url = 'https://chatu-3abd7.firebaseio.com/'
ref = db.reference('/',None,db_url)

def request_notification(event, context):

    request_key = context.resource.split('/')[-1]
    action = event.get('delta').get('action')
    data = event.get('delta').get('data')
    
    if(action == 'register'):
        user_id = data.get('uid')
        user_name = data.get('name')
        registration_token = data.get('token')
        uid = ref.child('uid/'+user_id+'/device_uid').get()
        if(uid != None):
            message = messaging.Message(
                data={
                    'action':'signin',
                    'token':registration_token,
                    'uid':uid,
                    'name':user_name
                },
                token=registration_token
            )
            response = messaging.send(message)
            ref.child('user/'+uid).update({'token':registration_token})
        else:
            uid = ''.join(choices(digits, k=8))
            message = messaging.Message(
                data={
                    'action':'register',
                    'token':registration_token,
                    'uid':uid,
                    'name':user_name
                },
                token=registration_token
            )
            response = messaging.send(message)
            ref.child('user/'+uid).set({'token':registration_token,'name':user_name})
            ref.child('uid/'+user_id).set({'device_uid':uid})

    elif(action == 'message'):
        from_uid = data.get('from_uid')
        to_uid = data.get('to_uid')
        content_type = data.get('type')
        content = data.get('content')
        time = data.get('time')

        token = ref.child('user/'+to_uid+'/token').get()
        name = ref.child('user/'+from_uid+'/name').get()
        message = messaging.Message(
            data={
                'action':'message',
                'name':name,
                'from_uid':from_uid,
                'to_uid':to_uid,
                'type':content_type,
                'content':content,
                'time':time
            },
            token=token
        )
        response = messaging.send(message)
    
    elif(action == 'invitation'):
        from_uid = data.get('from_uid')
        to_uid = data.get('to_uid')
        name = data.get('name')
        time = data.get('time')

        token = ref.child('user/'+to_uid+'/token').get()
        message = messaging.Message(
            data={
                'action':'invitation',
                'from_uid':from_uid,
                'to_uid':to_uid,
                'name':name,
                'time':time
            },
            token=token
        )
        response = messaging.send(message)

    elif(action == 'reply'):
        from_uid = data.get('from_uid')
        to_uid = data.get('to_uid')
        name = data.get('name')
        time = data.get('time')
        answer = data.get('answer')

        token = ref.child('user/'+to_uid+'/token').get()
        message = messaging.Message(
            data={
                'action':'reply',
                'from_uid':from_uid,
                'to_uid':to_uid,
                'name':name,
                'time':time,
                'answer':answer
            },
            token=token
        )
        response = messaging.send(message)

    elif(action == 'find'):
        from_uid = data.get('from_uid')
        to_uid = data.get('to_uid')
        
        refUid = ref.child('user').get()
        if(to_uid in refUid):
            name = refUid.get(to_uid).get('name')
        else:
            name = ""
        token = ref.child('user/'+from_uid+'/token').get()
        message = messaging.Message(
            data={
                'action':'find',
                'from_uid':from_uid,
                'to_uid':to_uid,
                'name':name,
            },
            token=token
        )
        response = messaging.send(message)

    else:
        print("Unknown Action!!!!!!!")
    
    ref.child('requests/'+request_key).delete()