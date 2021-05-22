package agency.highlysuspect.apathy.expr;

import org.derive4j.Data;

import java.util.function.Function;

//What is this, Rust?
@Data
abstract class Result<OK, ERR> {
	public abstract <R> R cata(Function<OK, R> ok, Function<ERR, R> err);
	
	public final boolean isOk() {
		return Results.getOk(this).isPresent();
	}
	
	public final OK get() {
		return Results.getOk(this).get();
	}
	
	public final boolean isErr() {
		return Results.getErr(this).isPresent();
	}
	
	public final ERR getErr() {
		return Results.getErr(this).get();
	}
}
