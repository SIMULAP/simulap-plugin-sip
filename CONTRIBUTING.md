__© Copyright 2018 Hewlett Packard Enterprise Development LP__

# Developer Certificate of Origin

The simulap-plugin-sip project uses a mechanism known as a Developer Certificate of Origin (DCO) to manage contribution process. The DCO is a legally binding statement that asserts that you are the creator of your contribution, and that you wish to allow simulap-plugin-sip to use your work.

Acknowledgement of this permission is done using a sign-off process in Git. The sign-off is a simple line at the end of the explanation for the patch. The text of the DCO is fairly simple (from developercertificate.org):


```
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
660 York Street, Suite 102,
San Francisco, CA 94110 USA

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.

Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

If you are willing to agree to these terms, you just add a line to every git commit message:
Signed-off-by: Joe Smith <joe.smith@email.com>


If you set your user.name and user.email as part of your git configuration, you can sign your commit automatically with git commit -s.

Unfortunately, you have to use your real name (i.e., pseudonyms or anonymous contributions cannot be made). This is because the DCO is a legally binding document, granting the simulap-plugin-sip project to use your work.



# GitHub workflow

## Prerequisistes 


* git must be installed on your __local__ system and you must be familiar with git CLI  : see https://git-scm.com/
* You must know how to fork, clone a repository on github  : see https://help.github.com/

 

## Roles

In the following, we distinguish three roles :

* __commiter__ :  Is in charge to write the __master__ branch of native blessed repository (SIMULAP/simulap-plugin-sip ) by reviewing ,accepting or rejecting GitHub pull requests ( contributions) .
* __integrator__ : is in charge to create, delete and write __release__ and __hotfix__ branches. __Integrator__ is also a __commiter__ since it has to merge __release__ and __hotfix__ branch on the __master__ before deleting them.
* __contributor__ : Is in charge of creating , writing and deleting __feature__ branches on  a public  forked repository of SIMULAP/simulap-plugin-sip . It also has to submit pull request to notify __commiter__ about his/her intention to contribute to native blessed repository ( SIMULAP/simulap-plugin-sip)



## Basic principles 

* every new production release is based on the previous one
* workflow uses only one eternal branch named __master__, which represent the stable under development branch
* Each feature must be made on a temporary feature branch . Assume feature name is "myFeature". The name of the branch must be __feature/myFeature__ . In the following , we will explain how a __contributor__  can  create, submit and finish a feature branch
* When current master branch state can be released, a temporary release branch is  created to prepare the release and control the changes. Assume release name is myRelease, the name of the branch must be __release/myRelease__ . Release branch are used to limit contribution for a specific release to bug fixes, while enabling contribution for next release on master branch. In the following , we will explain how an integrator can create, write, and finish a release branch
* When a critical defect is found in last release, which needs to be fixed as soon as possible, a temporary hotfix branch is created which will result in a new unplanned release. Assume myRelease is the name of the unplanned release, the name of the branch must be __hotfix/myRelease__. In the following we will explain how an __integrator__ can create, write and finish a hotfix branch 


## Contributor


#### __Step 1__ : fork https://github.com/SIMULAP/simulap-plugin-sip

You obtain a new public github repository  : https://github.com/MY_USERNAME/simulap-plugin-sip .
In the following , https://github.com/SIMULAP/simulap-plugin-sip is called the __upstream__ while https://github.com/MY_USERNAME/simulap-plugin-sip is called the __origin__

#### __Step 2__ : create a local clone of https://github.com/MY_USERNAME/simulap-plugin-sip

```
prompt> git clone  git@github.com:MY_USERNAME/simulap-plugin-sip.git
```

#### __Step 3__ : add upstream as a remote repository 
 
 ```
prompt> git add remote upstream  git@github.com:SIMULAP/simulap-plugin-sip.git
prompt> git remote -v    # list the remote

```

#### __Step 4__ : Create a local feature branch

```
prompt> git checkout -b feature/myFeature master
```

#### __Step 5__ : commit everything needed on local feature/myFeature branch to implement the feature

git command to be used are :
* git status
* git add
* git commit

#### __Step 6__ : rebase local feature/myFeature branch from upstream/master then push to origin/feature/MyFeature 

```
prompt> git checkout master                       # set master as the current branch
prompt> git pull upstream master                  # update local master branch from upstream master branch
prompt> git checkout  feature/myFeature           # set feature/myFeature as the current branch
prompt> git rebase -i  master                     # rebase local feature/myFeature branch from top of local master branch 
```

#### __step 7__ : commit locally then push to origin

```
prompt> git push origin feature/myFeature         # push branch to origin
 
```
 

#### __step 8__ : create a pull request on origin.

A pull request is a relationship object between  "feature/myFeature branch on origin" and "master branch on upstream".
Contributor has to create the pull request on origin. It automatically propagates the pull request on upstream, and notify upstream committers so as she/he can review it and treat it .

To create the pull request on origin, please navigate to https://github.com/MY_USERNAME/simulap-plugin-sip .
A new button __Compare & pull request__ appeared under __code__ tab which offers the possibility to create a pull request for your newly pushed branch feature/myFeature.
A comparison will be made with upstream/master branch to see if merge is possible.
You have the possibility to enter comments, to explain why your code is worth merging with upstream/master branch.
Then click on __Create pull request__ button.

At this stage, committers on upstream are notified by email.

__BE CAREFULL__ : since feature/myFeature is in relationship with a Pull request on upstream under review, you MUST NOT use rebase from master on your branch starting from pull request creation. A rebase has a side effect. It rebuids the commits you made on your branch  before the rebase. As a consequence, the reviewers can be confused with these changes

#### __step 9__ : modify feature/myFeature branch on origin, until comitters on master refuse to merge the branch

It is the responsability of committer to decide if changes made on feature/myFeature branch on origin must be merged with master branch on upstream. Committer can have a dialog with contributor through comments on pull request.
If requested by committer, the contributor may have to rework the content of feature/myFeature branch on origin by committing new changes. The committer will automatically be aware of that . 

#### __step 10__ : pull request is merged on upstream master branch by committer. You must now cleanup your local feature/myFeature branch as well as the remote feature/myFeature branch on origin

At this stage, your pull request has been accepted/merged by upstream committer. It's time for contributor to delete feature/myFeature branch locally and on origin. 


```
prompt> git push origin --delete feature/myFeature # delete branch feature/myFeauture on origin
prompt> git checkout master                        # set master as current branch to be able to delete another branch
prompt> git branch -D  feature/myFeature     # delete local branch feature/myFeature

```

If contributor wants to work on a new feature, he has to come back to step 4 to create a new feature branch locally.



## Integrator


### Manage Release branch

As a committer, integrator can directly clone the native blessed repository https://github.com/SIMULAP/simulap-plugin-sip.  No needs to fork a repository on github first. In the following, native blessed repository https://github.com/SIMULAP/simulap-plugin-sip will be called __origin__ while clone will be called __local__.

#### __Step 1__ : create a local clone of https://github.com/SIMULAP/simulap-plugin-sip

```
prompt> git clone  git@github.com:SIMULAP/simulap-plugin-sip.git # create a local copy of https://github.com/SIMULAP/simulap-plugin-sip
```

#### __Step 2__ : create a local release branch release/myRelease from local master branch

```
prompt> git checkout -b release/myRelease master # create a local branch starting from the current state of local master branch

```

#### __Step 3__ : prepare release on local release/myRelease and commit changes

Integrator can have to change things related to versionning itself. It can be the case when defaut packaging version is hardcoded for instance.
Integrator will use "git status", "git add"", and "git commit" commands to commit change locally on release/myRelease.

#### __Step 4__ : push local branch release/myRelease to origin

This step is needed if contributors other than integrator have to do a last minute contribution to release/myRelease branch on origin

```
prompt> git push origin release/myRelease  # push release/myRelease to origin

```
#### __Step 5__ : when state of release/myRelease is satisfying, tag the branch with myRelease Tag, then push the branch to origin

```
prompt> git checkout release/myRelease            # switch to release/myRelease branch
prompt> git pull                                  # get last change from contributors on origin release/myRelease
prompt> git tag myRelease                         # tag current branch state with label myRelease 
prompt> git push --tags origin release/myRelease  # push release/myRelease with its tags to origin

```

#### __Step 6__ : create a pull request on origin

To create the pull request on origin, please navigate to https://github.com/SIMULAP/simulap-plugin-sip .
A new button __Compare & pull request__ appeared under __code__ tab which offers the possibility to create a pull request for your newly pushed branch release/Release.
A comparison will be made with origin/master branch to see if merge is possible.
You have the possibility to enter comments, to explain why your code is worth merging with upstream/master branch. It can be for instance a summary of myRelease content.
Then click on __Create pull request__ button.

At this stage, committers on origin are notified by email.

#### __Step 7__ : accept pull request as committer  upstream

As integrator , you're  a committer. As such, you will accept your own pull request to merge release/myRelease branch and master branch on origin.

__BE CAREFUL__ : sometime, treating a pull request need a sequence of "pull request" action. Not necessarilly one.
It's the case when ( in case of conflict  ) github proposew first to merge master into the branch.
If it's the case, a second step is needed. Merging the branch into master.

#### __Step 8__ : delete local and origin branch

As stated in basic principles, a release branch is temporary. the myRelease tag, created on release/myRelease branch was merged on master branch. It means, you can at any moment in the future, recover the state of myRelease, by checkouting myRelease tag from master.
As a consequence, there is no need to keep release/myRelease branch alive. You must delete it .

```
prompt> git push origin --delete release/myRelease # delete branch release/myRelease on origin
prompt> git checkout master                        # set master as current branch to be able to delete another branch
prompt> git branch -D  release/myRelease     # delete local branch release/myRelease

```

### Manage hot fix branch

A hot fix branch is a temporary branch, created  from a TAG.
The goal is to bring a hot fix on top of an already delivered release, without disturbing the contribution on master branch.
The process is similar to __"manage release branch"__ except the following points :

* the branch must be named hotfix/myHotfixRelease : myHotfixRelease being the tag under which the hot fix content will be labelled
* the branch is not created from the tip of master branch. It is created from a tag . The tag pointing to a commit, itself corresponding to a release.

Assume __myTagRelease__ is the release on top of which we want to deliver a hot fix.
Assume __myHotfixRelease__ is the release with which we will deliver the hotfix.
Here is the way to create the branch at step 1:


 ```
prompt> git checkout master                               # switch to master branch
prompt> git fetch --prune --tags                          # fetch meta data from origin
prompt> git tag                                           # list the tags available. myTagRelease must belong to the list                
prompt> git checkout hotfix/myHotfixRelease myTagRelease  # create a local branch starting from myTagRelease of branch master.

```

Starting from this point, everything must be managed as for __"Manage release branch"__ .
Of course, the tag created at step 5 is : __myHotfixRelease__
