package org.grails.twitter

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable

import org.grails.twitter.auth.Person

class StatusService {

    static expose = ['jms']

    def springSecurityService
    def timelineService

    void onMessage(newMessageUserName) {
        log.debug "Message received. New status message posted by user <${newMessageUserName}>."
        def following = Person.where { followed.username == newMessageUserName }.property('username').list()
        following.each { uname ->
            timelineService.clearTimelineCacheForUser(uname)
        }
    }

    void updateStatus(String message) {
        def status = new Status(message: message)
        status.author = lookupCurrentPerson()
        status.save()
        timelineService.clearTimelineCacheForUser(status.author.username)
    }

    /* Change the "followed" status for the specified person for the current user.  
     * If the personId is for a user already in the current user's "followed" set, 
     * then remove the person from the set, otherwise add them to the set.
     * 
     * @param personId  the id of the person whose followed status should be toggled
     * */

    void toggleFollow(long personId) {

        def person = Person.get(personId)
        if (person) {
            Person currentUser = lookupCurrentPerson()
            if (isFollowed(personId)){
                currentUser.removeFromFollowed(person)
            }
            else{
                currentUser.addToFollowed(person)
            }

            timelineService.clearTimelineCacheForUser(currentUser.username)
        }
    }


    boolean isFollowed(long personId) {
        def person = Person.get(personId)
        // println("\n=========  person is " + personId+ " " + person)
        if (person) {
            def currentUser = lookupCurrentPerson()
            Set followingUsers = currentUser.getFollowed()
            // println "---following: " + followingUsers
            followingUsers.contains(person)
        }
    }

    private lookupCurrentPerson() {
        Person.get(springSecurityService.principal.id)
    }
}
