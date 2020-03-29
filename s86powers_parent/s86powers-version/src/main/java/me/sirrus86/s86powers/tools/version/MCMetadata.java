package me.sirrus86.s86powers.tools.version;

public enum MCMetadata {
	
	BAT_HANGING((byte) 0x00, 12, 14, 15),
	TAMEABLE_STATE((byte) 0x00, 13, 15, 16);
	
	private Object defValue;
	private int index1_13, index1_14, index1_15;
	
	private <O> MCMetadata(Object defValue, int index1_13, int index1_14, int index1_15) {
		this.defValue = defValue;
		this.index1_13 = index1_13;
		this.index1_14 = index1_14;
		this.index1_15 = index1_15;
	}
	
	public Object getDefaultValue() {
		return this.defValue;
	}
	
	public int getIndex() {
		switch(MCVersion.CURRENT_VERSION) {
		case v1_13: case v1_13_1: case v1_13_2:
			return index1_13;
		case v1_14: case v1_14_1: case v1_14_2: case v1_14_3: case v1_14_4:
			return index1_14;
		case v1_15: case v1_15_1: case v1_15_2:
			return index1_15;
		default:
			return -1;
		}
	}

}
