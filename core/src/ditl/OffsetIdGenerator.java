package ditl;

public final class OffsetIdGenerator implements IdGenerator {

	private int min_id = 0;
	
	public OffsetIdGenerator(int minId){ min_id = minId; }
	
	@Override
	public Integer getInternalId(String strId) {
		return Integer.parseInt(strId)+min_id;
	}

	@Override
	public void writeTraceInfo(Writer<?> writer) {} // nothing to do
}
