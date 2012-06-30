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
        statusService.toggleFollow(id)
        redirect action: 'index'
    }
}
