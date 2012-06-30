package org.grails.twitter

import grails.plugins.springsecurity.Secured

import org.grails.twitter.auth.Person

@Secured('IS_AUTHENTICATED_FULLY')
class StatusController {

    def statusService
    def timelineService
    def springSecurityService

    def index() {
        def messages = timelineService.getTimelineForUser(springSecurityService.principal.username)
        [statusMessages: messages]
    }

    def updateStatus(String message) {
        statusService.updateStatus message
        def messages = timelineService.getTimelineForUser(springSecurityService.principal.username)

        def content = twitter.renderMessages messages: messages
        render content
    }

    def toggleFollow(long id) {
        if (statusService.isfollowed(id))
            statusService.unfollow(id)
        else
            statusService.follow(id)
        redirect action: 'index'
    }

    def follow(long id) {
        statusService.follow id
        redirect action: 'index'
    }
    def unfollow(long id) {
        statusService.unfollow id
        redirect action: 'index'
    }
}
