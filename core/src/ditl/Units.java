package ditl;

public final class Units {

	public static Long getTicsPerSecond(String timeUnit) {
		String unit = timeUnit.toLowerCase();
		if ( unit.equals("s") ){
			return 1L;
		} else if ( unit.equals("ms") ){
			return 1000L;
		} else if ( unit.equals("us") ){
			return 1000000L;
		} else if ( unit.equals("ns") ){
			return 1000000000L;
		}
		return null;
	}
	
	public static String toTimeUnit(long tps) {
		if ( tps == 1L ){
			return "s";
		} else if ( tps == 1000L ) {
			return "ms";
		} else if ( tps == 1000000L ){
			return "us";
		} else if ( tps == 1000000000L ){
			return "ns";
		}
		return null;
	}
}
