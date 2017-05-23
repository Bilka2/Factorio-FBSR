package com.demod.fbsr.render;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;

import com.demod.factorio.DataTable;
import com.demod.factorio.prototype.DataPrototype;
import com.demod.fbsr.BlueprintEntity;
import com.demod.fbsr.BlueprintEntity.Direction;
import com.demod.fbsr.WorldMap;

public class BoilerRendering extends TypeRendererFactory {

	@Override
	public void createRenderers(Consumer<Renderer> register, WorldMap map, DataTable dataTable, BlueprintEntity entity,
			DataPrototype prototype) {
		List<Sprite> sprites = getSpritesFromAnimation(prototype.lua().get("structure"), entity.getDirection());
		register.accept(spriteRenderer(sprites, entity, prototype));
	}

	public boolean pipeFacingMeFrom(Direction direction, WorldMap map, BlueprintEntity entity) {
		return map.isPipe(direction.offset(entity.getPosition()), direction.back());
	}

	@Override
	public void populateWorldMap(WorldMap map, DataTable dataTable, BlueprintEntity entity, DataPrototype prototype) {
		Direction dir = entity.getDirection();
		Point2D.Double position = dir.back().offset(entity.getPosition(), 0.5);
		map.setPipe(dir.offset(position, 1), dir);
		map.setPipe(dir.left().offset(position, 1), dir.left());
		map.setPipe(dir.right().offset(position, 1), dir.right());

		if (!prototype.lua().get("energy_source").isnil()) {
			map.setHeatPipe(position, dir.back());
		}
	}

}
