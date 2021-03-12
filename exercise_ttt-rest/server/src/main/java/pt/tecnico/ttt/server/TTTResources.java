package pt.tecnico.ttt.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.tecnico.ttt.contract.PlayRequest;
import pt.tecnico.ttt.contract.PlayResult;

/**
 * Root resource (exposed at "game" path)
 */
@Path("game")
public class TTTResources {
	
	/**
	 * 
	 * TTTGame currently being played
	 * 
	 */
	static TTTGame game = new TTTGame();

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return Board that will be returned as a text/plain response.
	 */ 
	@GET
	@Path("board")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBoard() {
		return game.toString();
	}
	
	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return Board that will be returned as a text/plain response.
	 */ 
	@GET
	@Path("board/reset")
	@Produces(MediaType.TEXT_PLAIN)
	public String resetBoard() {
		game.resetBoard();
		return game.toString();
	}
	
	
	@GET
	@Path("board/checkwinner")
	@Produces(MediaType.TEXT_PLAIN)
	public int checkWinner() {
		return game.checkWinner();
	}

	@POST
	@Path("play")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public PlayResult play(PlayRequest request) {
		return game.play(request.getRow(), request.getColumn(), request.getPlayer());
	}

	@GET
	@Path("play/{arg1}/{arg2}/{arg3}")
	@Produces({MediaType.APPLICATION_JSON})
	public PlayResult metodo(@PathParam("arg1") int arg1,
					@PathParam("arg2") int arg2, @PathParam("arg2") int arg3) {
		return game.play(arg1, arg2, arg3);
	}
}
