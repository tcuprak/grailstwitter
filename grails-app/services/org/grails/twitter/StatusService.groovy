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

    void follow(long personId) {

        def person = Person.get(personId)
        Person currentUser = lookupCurrentPerson()
        println("\nfollowing =========  person is " + personId+ " " + person)
        if (person) {

            println "was following: " + currentUser.getFollowed()
            currentUser.addToFollowed(person)
            println "now following: " + currentUser.getFollowed()
        }

        timelineService.clearTimelineCacheForUser(currentUser.username)
    }

    void unfollow(long personId) {

        Person person = Person.get(personId)
        println("\nUnfollow=========  person is " + personId+ " " + person)
        Person currentUser = lookupCurrentPerson()
        if (person) {

            println "was following: " + currentUser.getFollowed()
            def unfollow = currentUser.removeFromFollowed(person)
            println "now following: " + currentUser.getFollowed()
        }
        timelineService.clearTimelineCacheForUser(currentUser.username)
    }

    boolean isfollowed(long personId) {
        def person = Person.get(personId)
        println("\n=========  person is " + personId+ " " + person)
        if (person) {
            def currentUser = lookupCurrentPerson()
            Set followingUsers = currentUser.getFollowed()
            println "---following: " + followingUsers
            followingUsers.contains(person)
        }
    }

    private lookupCurrentPerson() {
        Person.get(springSecurityService.principal.id)
    }
}
