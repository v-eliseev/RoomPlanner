package roomplanner

import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.XmlSolverFactory
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.impl.score.director.ScoreDirector

import roomplanner.api.RoomCategory as RoomCategoryDto
import roomplanner.api.Room as RoomDto
import roomplanner.api.Reservation as ReservationDto
import roomplanner.api.RoomAssignment as RoomAssignmentDto
import roomplanner.api.Plan as PlanDto
import roomplanner.api.Score as ScoreDto

import org.apache.commons.logging.LogFactory

class SolverHelper {

	private static final log = LogFactory.getLog(this.getClass())

	static void convertFromDto (
		List<RoomCategoryDto> roomCategoriesDto, 
		List<RoomDto> roomsDto, 
		List<ReservationDto> reservationsDto, 
		List<RoomAssignmentDto> roomAssignmentsDto,
		def roomCategories, 
		def rooms, 
		def reservations, 
		def roomAssignments
	) {
		log.debug("Before conversion...")
		roomCategories = roomCategoriesDto?.collect { roomCategory ->
			log.debug("Converting RoomCategories")
			new RoomCategory( 
				id: roomCategory.id
			) 
		}
		rooms = roomsDto?.collect { room ->
			log.debug("Converting Rooms")
			new Room( 
				id: room.id,
				roomCategory: roomCategories.find { it.id == room.roomCategory.id },
				adults: room.adults
			) 
		}
		reservations = reservationsDto?.collect { reservation ->
			log.debug("Converting Reservations")
			new Reservation(
				id: reservation.id,
				roomCategory: roomCategories.find { it.id == reservation.roomCategory.id },
				adults: reservation.adults,
				bookingInterval: reservation.bookingInterval
			) 
		}
		roomAssignments = roomAssignmentsDto?.collect { roomAssignment ->
			log.debug("Converting roomAssignments")
			new RoomAssignment( 
				id: roomAssignment.id,
				room: rooms.find { it.id == roomAssignment.room.id },
				reservation:  reservations.find { it.id == roomAssignment.reservation.id },
				moveable: false
			) 
		}
		
		if (rooms == null) { rooms = new ArrayList<Room>() }
		if (roomCategories == null) { roomCategories = new ArrayList<RoomCategory>() }
		if (reservations == null) { reservations = new ArrayList<Reservation>() }
		if (roomAssignments == null) { roomAssignments = new ArrayList<RoomAssignment>() }

	}

	static PlanDto buildDtoResponse(Schedule solvedSchedule) {
		PlanDto planDto = new PlanDto()

		planDto.score = new ScoreDto(
			 feasible: solvedSchedule.score.feasible,
			 hardScoreConstraints: solvedSchedule.score.hardScore,
			 softScoreConstraints: solvedSchedule.score.softScore
			 //scoreDetails: solvedSchedule.getScoreDetailList()   
		)
		planDto.roomAssignments = []
		solvedSchedule.roomAssignments.each { roomAssignment ->
		planDto.roomAssignments <<
			new RoomAssignmentDto(
				id: roomAssignment.id,
				room: roomsDto.find { it.id == roomAssignment.room.id },
				reservation: reservationsDto.find { it.id == roomAssignment.reservation.id },
				moveable: roomAssignment.moveable
				)
		}

		planDto
	}

	static Schedule solveProblem(def grailsApplication, def roomCategories, def rooms, def reservations, def roomAssignments) {

		Schedule solvedSchedule = null
		try {

			Schedule unsolvedSchedule = new Schedule()

			log.trace("Add problem facts")

			unsolvedSchedule.rooms.addAll(rooms)
			unsolvedSchedule.roomCategories.addAll(roomCategories)
			unsolvedSchedule.reservations.addAll(reservations)
			unsolvedSchedule.roomAssignments.addAll(roomAssignments)
			createRoomAssignmentList(unsolvedSchedule)	
			
			synchronized (this) {
				Solver solver = null

				def configValue = grailsApplication.config.solverObject

				if (configValue) {
					log.trace("Get solver from applicationContext")	 
					solver = configValue
				} else {
		    		log.trace("Configure solver")
					SolverFactory solverFactory = new XmlSolverFactory()
					
					try {
						solverFactory.configure(grailsApplication.config.solver.configurationXML)
					} catch (Exception e) {
						log.error("Cannot configure solver: " + e.message)
						throw new Exception()
					}
					
		    		log.trace("Build solver")
					solver = solverFactory.buildSolver()

		    		log.trace("Build scoreDirector")
					ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();

		    		log.trace("Store solver in grailsApplication")
					grailsApplication.config.solverObject = solver
					grailsApplication.config.scoreDirectorObject = scoreDirector
				}

				unsolvedSchedule.scoreDirector = grailsApplication.config.scoreDirectorObject
				 
				log.trace("Start solving")

				unsolvedSchedule.scoreDirector.setWorkingSolution(unsolvedSchedule);
				solver.setPlanningProblem(unsolvedSchedule);
				solver.solve();
			 
				solvedSchedule = (Schedule) solver.getBestSolution();

				log.trace("Get constraints info")
				solvedSchedule.scoreDirector = grailsApplication.config.scoreDirectorObject
				solvedSchedule.scoreDirector.setWorkingSolution(solvedSchedule)
				solvedSchedule.scoreDirector.calculateScore()
			} // synchronized
		} catch (Exception e) {
			log.error("Error solving", e)
		}
		solvedSchedule
	}

	static private void createRoomAssignmentList(Schedule schedule) {
	List<Reservation> reservationList = schedule.reservations;
	List<RoomAssignment> roomAssignmentList = new ArrayList<RoomAssignment>(reservationList.size());
	long id = 0L;
	reservationList.each() { reservation ->
		RoomAssignment roomAssignment = new RoomAssignment();
		roomAssignment.id = id;
		id++;
		roomAssignment.reservation = reservation;
		roomAssignment.moveable = true;
		// Notice that we leave the PlanningVariable properties on null
		roomAssignmentList.add(roomAssignment);
	}
	schedule.roomAssignments.addAll(roomAssignmentList);
}

}