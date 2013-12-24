package roomplanner

import command.ListParams

class AdminController {

	def adminService

    def index() {
    	
    	def requestInstanceList = adminService.getRecentRequestList()
    	def statusMap = adminService.getStatus()
    	[
    		requestInstanceList: requestInstanceList,
    		status: statusMap
    	]
    }

    def showRequestDetail() {
        def requestInstance = PlannerRequest.get(params.id)
        if (!requestInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                message(code: 'license.label', default: 'Request'),
                params.id
            ])
            redirect(action: "index")
            return
        }

        log.debug(requestInstance)
        
        [requestInstance: requestInstance]
    }
}
