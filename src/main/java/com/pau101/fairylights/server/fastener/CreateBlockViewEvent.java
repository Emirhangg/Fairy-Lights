package com.pau101.fairylights.server.fastener;

import net.minecraftforge.fml.common.eventhandler.Event;

public class CreateBlockViewEvent extends Event {
	private BlockView view;

	public CreateBlockViewEvent(final BlockView view) {
		this.view = view;
	}

	public BlockView getView() {
		return this.view;
	}

	public void setView(final BlockView view) {
		this.view = view;
	}
}
