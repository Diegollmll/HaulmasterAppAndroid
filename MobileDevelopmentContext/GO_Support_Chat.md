Thread





Daniel Schütz
  Feb 5th at 10:40 AM
I have one question, there is a way to extend the User Fields? I have 2 roles Admin and Operator both can be users so, Can I extend the main User adding custom fields?




248 replies


Walter Angelo Almeida
  Feb 5th at 10:45 AM
for this you would not extend the user fields but rather use application roles. This works if you don't need to have separate user data of admin and operator; would that be the case ? if it is, can you give me an example ?


Daniel Schütz
  Feb 5th at 10:55 AM
I need the Admin for platform administration and can access to register and fills data of entities, but the operator access to some features only and operator has some extra fields so I relate it to other entities, but both are Users


Walter Angelo Almeida
  Feb 5th at 10:56 AM
yes, then having applications roles will be enough


Daniel Schütz
  Feb 5th at 10:56 AM
So for example: an Admin can register vehicles but the operator can use those vehicles


Walter Angelo Almeida
  Feb 5th at 10:56 AM
you can create security rules based on roles for that. I will show you.


Daniel Schütz
  Feb 5th at 10:57 AM
but in some point of the flow some users are not an operator yet


Walter Angelo Almeida
  Feb 5th at 10:57 AM
what is your status at the moment ? how much have you modelled on GO so far ? entities, fields and relations .
10:57
?


Daniel Schütz
  Feb 5th at 10:58 AM
So they became operator after the Training or the approval  of an admin after they did upload their certificates or present a quizz


Walter Angelo Almeida
  Feb 5th at 10:59 AM
ok, got it for the operator. so indeed problably more than just a role


Daniel Schütz
  Feb 5th at 11:00 AM
So do I have to Register as and Operator with a pending flag or he stills as user and then becames operator?


Walter Angelo Almeida
  Feb 5th at 11:00 AM
well there are different ways to do, we need to see pros and cons


Daniel Schütz
  Feb 5th at 11:01 AM
ok


Walter Angelo Almeida
  Feb 5th at 11:01 AM
but maybe not a priority right now. For now can be good to have all main entities and relationship defined so that we an auto create a first version of the back office.
11:01
and from there you could start building a first version of mobile application for one or two user stories.
11:02
I'd like to see already a first validation of the full tech stack, from front to back. When that's done then you can iterate to finalize all the use cases
11:02
how do you feel about this ? (edited) 


Daniel Schütz
  Feb 5th at 11:07 AM
ok, I created the most entities and properties, I was clarifying some points with the relationships and with Norman,  I will abstract that part so I’m focused on the first version and all main relationships. Yes we need the validation, sure! feels good
11:08
I will send you an update of the relationships as soon as possible


Daniel Schütz
  Feb 5th at 8:06 PM
Hey Walter, please check up this error, was when I tried to Auto create an application:
SolidDocumentName4_45_, ormentityd0_1_.[SolidDocumentPath] as SolidDocumentPath5_45_, ormentityd0_1_.[EntityTypeNamespaceId] as EntityTypeNamespaceId6_45_, ormentityd0_2_.[CountParameter] as CountParameter2_46_, ormentityd0_2_.[DeleteGenerationType] as DeleteGenerationType3_46_, ormentityd0_2_.[DeleteURL] as DeleteURL4_46_, ormentityd0_2_.[GetCollectionGenerationType] as GetCollectionGenerationType5_46_, ormentityd0_2_.[GetCollectionResultContainer] as GetCollectionResultContainer6_46_, ormentityd0_2_.[GetcollectionURL] as GetcollectionURL7_46_, ormentityd0_2_.[GetGenerationType] as GetGenerationType8_46_, ormentityd0_2_.[GetResultContainer] as GetResultContainer9_46_, ormentityd0_2_.[GetURL] as GetURL10_46_, ormentityd0_2_.[Pagenumberparameter] as Pagenumberparameter11_46_, ormentityd0_2_.[Pagesizeparameter] as Pagesizeparameter12_46_, ormentityd0_2_.[SaveGenerationType] as SaveGenerationType13_46_, ormentityd0_2_.[SaveResultContainer] as SaveResultContainer14_46_, ormentityd0_2_.[UpdateURL] as UpdateURL15_46_, ormentityd0_3_.[DocumentLibraryDescription] as DocumentLibraryDescription2_47_, ormentityd0_3_.[DocumentLibraryName] as DocumentLibraryName3_47_, ormentityd0_3_.[WebAppUrl] as WebAppUrl4_47_, ormentityd0_6_.[DefaultGraphURI] as DefaultGraphURI2_82_, ormentityd0_6_.[Defaultlang] as Defaultlang3_82_, ormentityd0_6_.[SPARQLEndPointURL] as SPARQLEndPointURL4_82_, ormentityd0_8_.[ExternalEntityId] as ExternalEntityId2_191_, ormentityd0_8_.[GOApplicationDefinitionId] as GOApplicationDefinitionId3_191_, ormentityd0_10_.[AuthenticationMode] as AuthenticationMode2_258_, ormentityd0_10_.[MicrosoftCrmUrl] as MicrosoftCrmUrl3_258_, ormentityd0_10_.[OrganizationName] as OrganizationName4_258_, ormentityd0_10_.[UserDomain] as UserDomain5_258_, ormentityd0_10_.[UserName] as UserName6_258_, ormentityd0_10_.[UserPassword] as UserPassword7_258_, ormentityd0_10_.[MicrosoftCrmEntityId] as MicrosoftCrmEntityId8_258_, ormentityd0_.[BusinessEntity_ItemId] as formula80_, ormentityd0_1_.[EntityTypeNamespaceId] as formula81_, ormentityd0_10_.[MicrosoftCrmEntityId] as formula346_, case when ormentityd0_1_.[Id] is not null then 1 when ormentityd0_2_.[Id] is not null then 2 when ormentityd0_3_.[Id] is not null then 3 when ormentityd0_4_.[Id] is not null then 4 when ormentityd0_5_.[Id] is not null then 5 when ormentityd0_6_.[Id] is not null then 6 when ormentityd0_7_.[Id] is not null then 7 when ormentityd0_8_.[Id] is not null then 8 when ormentityd0_9_.[Id] is not null then 9 when ormentityd0_10_.[Id] is not null then 10 when ormentityd0_.[Id] is not null then 0 end as clazz_ from [dbo].[EntityDataSource] ormentityd0_ left outer join [dbo].[SolidPodDataSource] ormentityd0_1_ on ormentityd0_.[Id]=ormentityd0_1_.[Id] left outer join [dbo].[JsonRestDataSource] ormentityd0_2_ on ormentityd0_.[Id]=ormentityd0_2_.[Id] left outer join [dbo].[SharePointDocLibDataSource] ormentityd0_3_ on ormentityd0_.[Id]=ormentityd0_3_.[Id] left outer join [dbo].[CustomDataSource] ormentityd0_4_ on ormentityd0_.[Id]=ormentityd0_4_.[Id] left outer join [dbo].[LocalDataSource] ormentityd0_5_ on ormentityd0_.[Id]=ormentityd0_5_.[Id] left outer join [dbo].[SPARQLEndPointDataSource] ormentityd0_6_ on ormentityd0_.[Id]=ormentityd0_6_.[Id] left outer join [dbo].[ActiveDirectoryDataSource] ormentityd0_7_ on ormentityd0_.[Id]=ormentityd0_7_.[Id] left outer join [dbo].[GODataSource] ormentityd0_8_ on ormentityd0_.[Id]=ormentityd0_8_.[Id] left outer join [dbo].[DatabaseDataSource] ormentityd0_9_ on ormentityd0_.[Id]=ormentityd0_9_.[Id] left outer join [dbo].[MicrosoftCrmDataSource] ormentityd0_10_ on ormentityd0_.[Id]=ormentityd0_10_.[Id] where ormentityd0_.[BusinessEntity_ItemId]=@p0]
(edited)
8:07
Maybe can be something left from my side in the entities
8:09
Is possible that can be the Calculated fields I set in the entities ?, something missing?
8:09
Thank you in advantage


Daniel Schütz
  Feb 5th at 8:15 PM
The connection does not support MultipleActiveResultSets
8:15
I removed the relationships
8:18
All relationships must be or just a few may work?


Daniel Schütz
  Feb 5th at 9:59 PM
In order to test, if the Country and States work, can I have just that relationships?. Thank you


Walter Angelo Almeida
  Feb 6th at 3:26 AM
Hi 
@Daniel Schütz
3:26
no removing of the relationships, please no use of auto create application before you have all relationships setup
3:27
tell me what is the status at the moment
3:29
I see there are no relationships anymore, I could restore the model before your deleted all the relationships so that you don't have to recreate them


Daniel Schütz
  Feb 6th at 7:48 AM
Hi 
@Walter Angelo Almeida
 !, I can’t create a relationship between Country and State, is giving me an error


Walter Angelo Almeida
  Feb 6th at 7:51 AM
can you give more details ? what is the error


Daniel Schütz
  Feb 6th at 7:51 AM
Now I could
7:51
Is weird


Walter Angelo Almeida
  Feb 6th at 7:51 AM
also, did you create many relationship before deleting them again ?
7:51
because I can restore the database before you deleted


Daniel Schütz
  Feb 6th at 7:51 AM
yesterday I couldn’t but today yes
7:52
no I just create the relationship between those 2 clases


Walter Angelo Almeida
  Feb 6th at 7:52 AM
ok
7:52
then you now can create all relationship. When done we can review before doing the auto-create
7:53
tell me if you have other issues in creating relations


Daniel Schütz
  Feb 6th at 7:53 AM
And then I had a problem with the CountryId, so I deleted the relationship, but then I couldn’t create one, but now yes (edited) 
7:53
Did you do something ?=


Walter Angelo Almeida
  Feb 6th at 7:53 AM
no I didn't do anything. Tell me if it happens again


Daniel Schütz
  Feb 6th at 7:53 AM
ok ok! Good


Walter Angelo Almeida
  Feb 6th at 7:54 AM
again, remember that you don't need to create manually FK like CountryId, they are auto created when you create the relation


Daniel Schütz
  Feb 6th at 7:54 AM
Some recommendation to restore the model when are troubles?
7:54
ok but when is autocreated the FK I need to change the type of the field?


Walter Angelo Almeida
  Feb 6th at 7:54 AM
restore has to be done on the database, on Azure .. you don't have access, need to ask


Daniel Schütz
  Feb 6th at 7:54 AM
to Guid?
7:55
or is good with “Relation” statement
7:55
?


Walter Angelo Almeida
  Feb 6th at 7:55 AM
I don't get your question


Daniel Schütz
  Feb 6th at 7:55 AM
ok
7:56
When I create a relationship, there is a field that is autocreated in the child right, so the type of the field is “Relation” but not Guid, this is converted then automatically or just with the Relationship may work?
8:00
Could I use the autocreation button after I did those basic relationships Country - State or need all relationships to test? Maybe is the same question but I want to be sure.


Walter Angelo Almeida
  Feb 6th at 8:08 AM
there is the relation field auto created but also the FK, you don't see it by default because it is a system field, you need to change the filter to display the system fields
8:09
image.png
 
image.png
8:09
so you see the CountryId1 that was auto created (because you already had a CountryId that you created manually)
8:10
so you can remove the CoutryId you created and rename CountryId1 to CountryId (both display name and internal name)
8:11
and in the future => no need to create the FK manually in entities, and if you did => delete them
Also sent to the channel


Daniel Schütz
  Feb 6th at 8:13 AM
ok ok let check! thank you!!
8:14
I deleted the field I created
Screenshot 2025-02-06 at 08.14.47.png
 
Screenshot 2025-02-06 at 08.14.47.png
8:15
and renamed the created from the system


Walter Angelo Almeida
  Feb 6th at 8:15 AM
:+1:


Daniel Schütz
  Feb 6th at 8:15 AM
2 files
 
Screenshot 2025-02-06 at 08.15.31.png
Screenshot 2025-02-06 at 08.15.40.png


Walter Angelo Almeida
  Feb 6th at 8:15 AM
now you can create all relationships


Daniel Schütz
  Feb 6th at 8:15 AM
ok ok
8:16
I will


Daniel Schütz
  Feb 6th at 8:37 AM
I’m creating this relationship
2 files
 
Screenshot 2025-02-06 at 08.33.43.png
Screenshot 2025-02-06 at 08.36.52.png
8:37
And give me this error
Screenshot 2025-02-06 at 08.37.17.png
 
Screenshot 2025-02-06 at 08.37.17.png


Walter Angelo Almeida
  Feb 6th at 8:38 AM
what happens if you save again ?


Daniel Schütz
  Feb 6th at 8:38 AM
gives me again
Screenshot 2025-02-06 at 08.38.23.png
 
Screenshot 2025-02-06 at 08.38.23.png


Walter Angelo Almeida
  Feb 6th at 8:38 AM
ok wait a min
8:38
try again


Daniel Schütz
  Feb 6th at 8:39 AM
ok going
8:39
Screenshot 2025-02-06 at 08.39.33.png
 
Screenshot 2025-02-06 at 08.39.33.png


Walter Angelo Almeida
  Feb 6th at 8:40 AM
ok let me have a look


Daniel Schütz
  Feb 6th at 8:40 AM
ok ok
8:40
but the relationship was created this time
8:41
Screenshot 2025-02-06 at 08.40.55.png
 
Screenshot 2025-02-06 at 08.40.55.png
8:41
So gives me the error but let create the relationship
8:41
what did you do?


Walter Angelo Almeida
  Feb 6th at 8:41 AM
yes , I think there is a bug for relation; work for first relation , and then second one not. I need to fix that
8:41
it is anoying
8:42
in the mean time, when this happen, you need to do a issreset on the server
8:42
do you access to the server ?


Daniel Schütz
  Feb 6th at 8:42 AM
ok, that was something that Claude recommends me but I don’t have access to the server


Walter Angelo Almeida
  Feb 6th at 8:43 AM
ok ask me or the team at root of channel to do it for you
8:43
and I will fix this issue asap


Daniel Schütz
  Feb 6th at 8:45 AM
Thank you!
8:45
now we know
8:45
so is recurrent, when I will be creating a second relationship the server must be restarted?


Walter Angelo Almeida
  Feb 6th at 8:46 AM
I am afraid so
8:46
that sucks :slightly_smiling_face:


Daniel Schütz
  Feb 6th at 8:46 AM
so every 2 relationships must reset


Walter Angelo Almeida
  Feb 6th at 8:47 AM
yes


Daniel Schütz
  Feb 6th at 8:47 AM
ok ok so we are very close in this task


Walter Angelo Almeida
  Feb 6th at 8:47 AM
still try to create without reset see what happens


Daniel Schütz
  Feb 6th at 8:47 AM
ok ok
8:47
going


Walter Angelo Almeida
  Feb 6th at 8:47 AM
for now I will be off for an hour, you can ask on the channel for someone to do the reset when you need


Daniel Schütz
  Feb 6th at 8:48 AM
ok ok
8:48
Thank you!
8:49
Have a great rest


Daniel Schütz
  Feb 6th at 9:04 AM
@Walter Angelo Almeida
 you know who can help me with that?


Walter Angelo Almeida
  Feb 6th at 11:02 AM
Hi 
@Daniel Schütz
 I am back if you need me


Walter Angelo Almeida
  Feb 6th at 11:17 AM
See message on basecamp, you can move to the next step of defining the mobile tech stack and architecture while I am fixing the issue for creating relations. Does not make sense that you lose time in creating relations in this clumsy way


Daniel Schütz
  Feb 6th at 11:50 AM
Thank you 
@Walter Angelo Almeida
, I was having a good time making connections and clarifying everything. Is really nice how the FK are created.
11:52
yes, I reach a few relationships but then again arises the error
2 files
 
Screenshot 2025-02-06 at 11.50.53.png
Screenshot 2025-02-06 at 11.52.06.png
11:52
I read, thanks
11:52
I will pause from here and jump to the mobile stack
:+1:
1



Walter Angelo Almeida
  Monday at 7:52 AM
Hi 
@Daniel Schütz
 I did not manage to reproduce the problem with creation of relationships on my local machine. Can you resume the creation of relationship, and ping me as soon as you have the issue ? I will then go debug on the server to see what is happening. Thank you


Daniel Schütz
  Monday at 12:49 PM
ok ok, I will retake this asap.
12:49
Thank you too


Walter Angelo Almeida
  Tuesday at 3:17 AM
Hi 
@Daniel Schütz
, yes tell me when you are ready for this, so that I can be available. Better beggining of your day


Daniel Schütz
  Tuesday at 8:15 AM
Hi 
@Walter Angelo Almeida
!
8:16
I’m available


Walter Angelo Almeida
  Tuesday at 8:24 AM
Hi Daniel,
8:25
then you can resume creating relations and tell me when you get an error


Daniel Schütz
  Tuesday at 8:34 AM
ok!


Walter Angelo Almeida
  Tuesday at 8:58 AM
I am on  call for an hour now, but please still share when you get an error, and I'll check on it after my call (edited) 


Daniel Schütz
  Tuesday at 9:22 AM
I have the error
9:24
Screenshot 2025-02-11 at 09.23.54.png
 
Screenshot 2025-02-11 at 09.23.54.png


Daniel Schütz
  Tuesday at 9:29 AM
8 files
 
Screenshot 2025-02-11 at 09.26.14.png
Screenshot 2025-02-11 at 09.26.30.png
Screenshot 2025-02-11 at 09.27.16.png
Screenshot 2025-02-11 at 09.27.35.png
Screenshot 2025-02-11 at 09.28.01.png
Screenshot 2025-02-11 at 09.28.22.png
Screenshot 2025-02-11 at 09.28.40.png
Screenshot 2025-02-11 at 09.28.55.png
9:30
Was creating this relationship:
Name: “Business_Country” (keep it clear and consistent)
Relation Type: “Many to One” (Since many Businesses can be in one Country)
Description: “Business located in Country” (optional but helpful)


Walter Angelo Almeida
  Tuesday at 10:26 AM
ok estou de volta
10:26
sorry I am speaking to you in portuguese :sweat_smile:
10:26
I am back, I'll check this now


Walter Angelo Almeida
  Tuesday at 10:38 AM
ok I will debug on the server (edited) 
10:39
please don't work on the model during this time


Walter Angelo Almeida
  Tuesday at 10:47 AM
OK I have change a setting in connection string to allow MultipleActiveResultSets
10:47
you can resume creating the relations 
@Daniel Schütz
 . Hopefully everything should be fine now. Tell me if you still have errors


Daniel Schütz
  Tuesday at 10:49 AM
jajaj no worries! Thank you, in the mean time I learn Portuguese
10:50
I had some friends from Portugal in Venezuela


Walter Angelo Almeida
  Tuesday at 10:50 AM
and Portuguese is not too far from Spanish :)


Daniel Schütz
  Tuesday at 10:51 AM
ok, :slightly_smiling_face:
10:51
I’m doing another
10:52
Screenshot 2025-02-11 at 10.52.07.png
 
Screenshot 2025-02-11 at 10.52.07.png


Walter Angelo Almeida
  Tuesday at 10:53 AM
ok let me check ...


Daniel Schütz
  Tuesday at 10:53 AM
was created
10:53
I’m editing the internal names
10:54
from items to Sites
10:54
I will save


Walter Angelo Almeida
  Tuesday at 10:54 AM
when this happens , please do to details and copy the full exception error


Daniel Schütz
  Tuesday at 10:54 AM
Screenshot 2025-02-11 at 10.54.14.png
 
Screenshot 2025-02-11 at 10.54.14.png
:+1:
1

Also sent to the channel


Daniel Schütz
  Tuesday at 10:54 AM
ok ok I will
10:54
Goes well
10:56
I will create other relationship


Daniel Schütz
  Tuesday at 11:11 AM
I modified the name of the entity to Singular
Screenshot 2025-02-11 at 11.11.18.png
 
Screenshot 2025-02-11 at 11.11.18.png
11:11
SiteCondition


Walter Angelo Almeida
  Tuesday at 11:12 AM
yes
11:12
this is the practice
11:12
also , you can have spaces in entity names, for example "Site condition" instead of "SiteCondition"


Daniel Schütz
  Tuesday at 11:12 AM
but in the SiteConditionSnapshot the calculated field stills calling the name as plural
Screenshot 2025-02-11 at 11.12.01.png
 
Screenshot 2025-02-11 at 11.12.01.png
11:13
ok


Walter Angelo Almeida
  Tuesday at 11:13 AM
better to have proper display name with spaces for entities. The internal name is auto set to not have spaces


Daniel Schütz
  Tuesday at 11:13 AM
Screenshot 2025-02-11 at 11.13.45.png
 
Screenshot 2025-02-11 at 11.13.45.png
11:14
Will no be problems since the name of the entity in the Calculated field didn’t take the change
11:14
?
11:14
Or is better to create the entity again?


Walter Angelo Almeida
  Tuesday at 11:15 AM
all should be good, the renames are stable. but let me check


Daniel Schütz
  Tuesday at 11:15 AM
or use it in plural?
11:15
ok  thank you


Walter Angelo Almeida
  Tuesday at 11:15 AM
image.png
 
image.png


Daniel Schütz
  Tuesday at 11:16 AM
a ok!
11:16
going


Walter Angelo Almeida
  Tuesday at 11:16 AM
this is because you have renamed the display name and not the internal name. Rename internal name
11:16
and give a proper display name like "Site condition" to all entities
11:16
what is this for : "SiteConditionSnapshot"
11:17
?


Daniel Schütz
  Tuesday at 11:17 AM
Great!
Screenshot 2025-02-11 at 11.16.53.png
 
Screenshot 2025-02-11 at 11.16.53.png
11:18
That SnapShot is to take the current conditions at the session time or when an incident ocurres
11:19
Solved the remane of an entity and changes from plural to singular
11:19
GOod
11:21
Screenshot 2025-02-11 at 11.21.22.png
 
Screenshot 2025-02-11 at 11.21.22.png
11:22
was the session


Walter Angelo Almeida
  Tuesday at 11:29 AM
oh this can be that your session is expired ... refresh the page and it will ask you to login again


Daniel Schütz
  Tuesday at 11:34 AM
yes
11:36
I see that when I opened different tabs with other functionalities such as entities or entity relations and enumerations at the same time the session is closed quickly


Walter Angelo Almeida
  Tuesday at 11:37 AM
this is not due to opening different tabs
11:37
the session time is short and should renew while the application is used, but it is not. I will fix that soon


Daniel Schütz
  Tuesday at 11:37 AM
a ok
11:37
session time


Daniel Schütz
  Tuesday at 11:52 AM
This block is related
Screenshot 2025-02-11 at 11.52.47.png
 
Screenshot 2025-02-11 at 11.52.47.png


Walter Angelo Almeida
  Tuesday at 11:54 AM
what do you mean by that ? (edited) 


Daniel Schütz
  Tuesday at 12:11 PM
did you restarted the server recently?
12:11
I mean I continue but I reach those relatioships


Walter Angelo Almeida
  Tuesday at 12:12 PM
I did not restart the server


Daniel Schütz
  Tuesday at 12:12 PM
ok ok
12:12
good


Walter Angelo Almeida
  Tuesday at 12:12 PM
still don't get what you mean when you say "I mean I continue but I reach those relatioships" :slightly_smiling_face:


Daniel Schütz
  Tuesday at 12:46 PM
I mean It seams that I found a way to avoid the error
12:46
because you did not reset the server anymore I hope (edited) 


Walter Angelo Almeida
  Tuesday at 1:07 PM
Oh ok got it now :grinning: so it seems like it is much better now :+1:


Daniel Schütz
  Tuesday at 1:07 PM
yes
Screenshot 2025-02-11 at 13.07.43.png
 
Screenshot 2025-02-11 at 13.07.43.png
1:08
I think is when I do something different than go straight to create a second relationship in sequence
1:09
I will test it more precisely


Daniel Schütz
  Tuesday at 1:16 PM
I created another without reset


Walter Angelo Almeida
  Tuesday at 1:45 PM
yes, so hopefully the bug is finished.
1:45
tell me when you are done with the data modelling and we will see together to review and then do the auto create application


Daniel Schütz
  Tuesday at 1:46 PM
yes with this alternative way
1:46
Thank you
1:46
I will let you know


Walter Angelo Almeida
  Tuesday at 1:51 PM
what do you mean with this alternative way ? you mean you still have the bug if you create one relation after the other ?


Daniel Schütz
  Tuesday at 1:52 PM
let me check
1:55
the bug didn’t appears
1:58
I created 2 relationships and didn’t appear
1:58
weird
1:59
but good too


Walter Angelo Almeida
  Tuesday at 1:59 PM
yes, I think should be good now. Just tell me if you see it happen again


Daniel Schütz
  Tuesday at 2:00 PM
ok


Daniel Schütz
  Yesterday at 1:46 PM
When would have I access  to the User entity and do the relationships with it?


Walter Angelo Almeida
  Yesterday at 2:08 PM
Oh yes
2:08
let me see to activate the security. I would have wanted to wait until we have a new version of GO, but that's ok
2:10
Ok , I have activated the security, you will have now the GOUser. You can have relationships with GOUser now
2:11
but if you would need to add fields to GOUser tell me before, because this is not recommended.
2:11
if you do one to one relationship with GOUser, keep GOUser as being the PK side (so that it does not add FK on GOUser but on the other side)
2:12
and is one to many or many to one relationships, better to have GOUser on the one side, for the same reason. If you need something different please tell me
2:14
Also, I don't know if you have done it already, but would be good to add all last versions of documents (ERD, MVP scope, specs, user stories etc.) on the Sub 7 project on basecamp


Daniel Schütz
  Yesterday at 9:21 PM
Thank you! I will take in care all you said, I will let you know when I finish all relationships except the user relations (edited) 
9:22
I was doing a lot of fixes and every thing must be ok when I finish.
9:25
Is good to see the system entities. Thank you


Daniel Schütz
  Yesterday at 9:37 PM
I will do that part of the user relations assap (edited) 


Daniel Schütz
  Today at 9:23 AM
I will be updating the documents with all changes assap, But yes I recently updated the folder with the ERD documentation, the other documents have to be uploaded


Daniel Schütz
  40 minutes ago
So is better I do an alternative table for the data related to the user operator and not to add fields to the entity
12:44
?
12:45
I thought that was possible resume the user something like this:
User {
        uuid Id PK “✓”
        uuid BusinessId FK “Optional”
        uuid DepartmentId FK “Optional”
        uuid ExperienceLevelId FK “Optional”
        string Username “✓”
        string Email “✓”
        string Password “✓”
        string FullName “✓”
        string Phone
        datetime Birthday
        boolean IsTrainer
        boolean IsCertified
        boolean IsAdmin
        int ExperienceYears
        int points
        string EmergencyContact
        string EmergencyPhone
        string MedicalConditions
        date LastMedicalCheck
        datetime LastLogin
        boolean IsActive
    }
12:46
But yes I saw that some fields are already created which is good, but if could add those fields that are not in the GOUser entity would be great
12:46
Screenshot 2025-02-13 at 12.46.35.png
 
Screenshot 2025-02-13 at 12.46.35.png
12:47
Those are the current fields, a few are according
12:47
but not the others
12:47
So I can solve it with an entity Operator or add the fields I need
12:48
@Walter Angelo Almeida
 Could you please give me your point of view?


Walter Angelo Almeida
  10 minutes ago
It is better you create a extra entity User where you can put all extra fields and create a relation one to one between User and GOUser with GOUser on PK side. something like this :
image.png
 
image.png


1:15
@Daniel Schütz
 can we arrange for a call tomorrow, around 4PM Portugal time ? The idea would be to do the auto create application together, and discuss about next step / start of developing the IOS app. Thank you
1:16
you should have a good enough ERD by now. There can always be improvments but they can be done on the way. we need start implementing now


Daniel Schütz
  7 minutes ago
yes sure!
1:18
Goit
1:18
Yes we will implement!
1:19
I will be  ready as much I consider
1:20
With your response I think I can continue and finish
1:20
Thank you!
1:22
Tomorrow is my Friday?


Walter Angelo Almeida
  1 minute ago
yes, friday for both of us


Daniel Schütz
  1 minute ago
ok!
1:23
:+1: