package agency.highlysuspect.apathy.expr;

import org.derive4j.Data;

import java.util.function.Function;

//What is this, Rust?
@Data
abstract class Result<OK, ERR> {
	public abstract <R> R cata(Function<OK, R> ok, Function<ERR, R> err);
	
	public abstract String toString();
	
	public final boolean isOk() {
		return Results.getOk(this).isPresent();
	}
	
	public final OK getOk() {
		return Results.getOk(this).get();
	}
	
	//Technically this is possible with derive4j's special sauce: "Results.modOk(Optional::of).apply(<a Result>)", for example.
	//But what happens is the type of "Results.modOk(Optional::of)" gets erased to requiring a Result<Object, Object>.
	//Then you can't actually use the function. Yay for Java's crap type inference.
	public final <OK2> Result<OK2, ERR> mapOk(Function<OK, OK2> mapper) {
		return cata(ok -> Results.ok(mapper.apply(ok)), Results::err);
	}
	
	public final boolean isErr() {
		return Results.getErr(this).isPresent();
	}
	
	public final ERR getErr() {
		return Results.getErr(this).get();
	}
}
