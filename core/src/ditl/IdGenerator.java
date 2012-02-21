package ditl;

public interface IdGenerator {
	public Integer getInternalId(String strId);
	public void writeTraceInfo(Writer<?> writer);
}
