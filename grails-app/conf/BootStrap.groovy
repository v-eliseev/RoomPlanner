
import org.drools.planner.config.SolverFactory
import org.drools.planner.config.XmlSolverFactory
import org.drools.planner.core.Solver
import org.drools.planner.core.score.director.ScoreDirector

class BootStrap {

	def grailsApplication

    def init = { servletContext ->

    		log.debug("Build solver")
			SolverFactory solverFactory = new XmlSolverFactory()
			
			try {
				InputStream xmlConfigStream = this.getClass().getResourceAsStream("/drools/roomScheduleSolverConfig.xml")
				solverFactory.configure(xmlConfigStream)
			} catch (Exception e) {
				log.error("Cannot configure solver: " + e.message)
				throw new Exception()
			}
			
			Solver solver = solverFactory.buildSolver()
			ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
			grailsApplication.config.solver = solver
			grailsApplication.config.scoreDirector = scoreDirector
			
			grailsApplication.config.startNanoTime = System.nanoTime()
    }


    def destroy = {
    }
}
