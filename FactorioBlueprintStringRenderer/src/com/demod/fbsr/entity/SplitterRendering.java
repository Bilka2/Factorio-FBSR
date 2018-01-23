package com.demod.fbsr.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.function.Consumer;

import com.demod.factorio.DataTable;
import com.demod.factorio.FactorioData;
import com.demod.factorio.prototype.EntityPrototype;
import com.demod.factorio.prototype.ItemPrototype;
import com.demod.fbsr.BlueprintEntity;
import com.demod.fbsr.Direction;
import com.demod.fbsr.EntityRendererFactory;
import com.demod.fbsr.RenderUtils;
import com.demod.fbsr.Renderer;
import com.demod.fbsr.Renderer.Layer;
import com.demod.fbsr.Sprite;
import com.demod.fbsr.WorldMap;

public class SplitterRendering extends EntityRendererFactory {

	private static final Path2D.Double markerShape = new Path2D.Double();
	static {
		markerShape.moveTo(-0.5 + 0.2, 0.5 - 0.125);
		markerShape.lineTo(0.5 - 0.2, 0.5 - 0.125);
		markerShape.lineTo(0, 0 + 0.125);
		markerShape.closePath();
	}

	@Override
	public void createRenderers(Consumer<Renderer> register, WorldMap map, DataTable dataTable, BlueprintEntity entity,
			EntityPrototype prototype) {
		int[] beltSpriteMapping = TransportBeltRendering.transportBeltSpriteMapping[entity.getDirection()
				.cardinal()][1];
		Sprite belt1Sprite = RenderUtils.getSpriteFromAnimation(prototype.lua().get("belt_horizontal"));
		belt1Sprite.source.y = belt1Sprite.source.height * beltSpriteMapping[0];
		if (beltSpriteMapping[1] == 1) {
			belt1Sprite.source.x += belt1Sprite.source.width;
			belt1Sprite.source.width *= -1;
		}
		if (beltSpriteMapping[2] == 1) {
			belt1Sprite.source.y += belt1Sprite.source.height;
			belt1Sprite.source.height *= -1;
		}
		Sprite belt2Sprite = new Sprite(belt1Sprite);

		Point2D.Double beltShift = entity.getDirection().left().offset(new Point2D.Double(), 0.5);

		belt1Sprite.bounds.x += beltShift.x;
		belt1Sprite.bounds.y += beltShift.y;
		belt2Sprite.bounds.x -= beltShift.x;
		belt2Sprite.bounds.y -= beltShift.y;

		Sprite sprite = RenderUtils.getSpriteFromAnimation(
				prototype.lua().get("structure").get(entity.getDirection().toString().toLowerCase()));

		register.accept(RenderUtils.spriteRenderer(Layer.ENTITY, belt1Sprite, entity, prototype));
		register.accept(RenderUtils.spriteRenderer(Layer.ENTITY, belt2Sprite, entity, prototype));
		register.accept(RenderUtils.spriteRenderer(Layer.ENTITY2, sprite, entity, prototype));

		Direction dir = entity.getDirection();
		Double pos = entity.getPosition();
		Point2D.Double leftPos = dir.left().offset(pos, 0.5);
		Point2D.Double rightPos = dir.right().offset(pos, 0.5);

		if (entity.json().has("input_priority")) {
			boolean right = entity.json().getString("input_priority").equals("right");
			Point2D.Double inputPos = dir.offset(right ? rightPos : leftPos, 0);

			register.accept(new Renderer(Layer.OVERLAY3, inputPos) {
				@Override
				public void render(Graphics2D g) throws Exception {
					AffineTransform pat = g.getTransform();

					Color color = Color.yellow;
					Color shadow = Color.darkGray;
					double shadowShift = 0.07;

					g.setTransform(pat);
					g.translate(inputPos.x, inputPos.y);
					g.rotate(dir.back().ordinal() * Math.PI / 4.0 + Math.PI);
					g.translate(shadowShift, shadowShift);
					g.setColor(shadow);
					g.fill(markerShape);

					g.setTransform(pat);
					g.translate(inputPos.x, inputPos.y);
					g.rotate(dir.back().ordinal() * Math.PI / 4.0 + Math.PI);
					g.setColor(color);
					g.fill(markerShape);

					g.setTransform(pat);
				}
			});
		}

		if (entity.json().has("output_priority")) {
			boolean right = entity.json().getString("output_priority").equals("right");
			Point2D.Double outputPos = dir.offset(right ? rightPos : leftPos, 0.6);

			if (entity.json().has("filter")) {
				String itemName = entity.json().getString("filter");
				Sprite spriteIcon = new Sprite();
				Optional<ItemPrototype> optItem = dataTable.getItem(itemName);
				if (optItem.isPresent()) {
					spriteIcon.image = FactorioData.getIcon(optItem.get());
					spriteIcon.source = new Rectangle(0, 0, spriteIcon.image.getWidth(), spriteIcon.image.getHeight());
					spriteIcon.bounds = new Rectangle2D.Double(-0.3 + (right ? 0.5 : -0.5), -0.3, 0.6, 0.6);

					Renderer delegate = RenderUtils.spriteRenderer(spriteIcon, entity, prototype);
					register.accept(new Renderer(Layer.OVERLAY2, delegate.getBounds()) {
						@Override
						public void render(Graphics2D g) throws Exception {
							g.setColor(new Color(0, 0, 0, 128));
							g.fill(spriteIcon.bounds);
							delegate.render(g);
						}
					});
				}
			} else {
				register.accept(new Renderer(Layer.OVERLAY3, outputPos) {
					@Override
					public void render(Graphics2D g) throws Exception {
						AffineTransform pat = g.getTransform();

						Color color = Color.yellow;
						Color shadow = Color.darkGray;
						double shadowShift = 0.07;

						g.setTransform(pat);
						g.translate(outputPos.x, outputPos.y);
						g.rotate(dir.back().ordinal() * Math.PI / 4.0 + Math.PI);
						g.translate(shadowShift, shadowShift);
						g.setColor(shadow);
						g.fill(markerShape);

						g.setTransform(pat);
						g.translate(outputPos.x, outputPos.y);
						g.rotate(dir.back().ordinal() * Math.PI / 4.0 + Math.PI);
						g.setColor(color);
						g.fill(markerShape);

						g.setTransform(pat);
					}
				});
			}
		}
	}

	@Override
	public void populateLogistics(WorldMap map, DataTable dataTable, BlueprintEntity entity,
			EntityPrototype prototype) {
		Direction dir = entity.getDirection();
		Double pos = entity.getPosition();
		Point2D.Double leftPos = dir.left().offset(pos, 0.5);
		Point2D.Double rightPos = dir.right().offset(pos, 0.5);

		setLogisticMove(map, leftPos, dir.frontLeft(), dir);
		setLogisticMove(map, leftPos, dir.frontRight(), dir);
		setLogisticMove(map, leftPos, dir.backLeft(), dir);
		setLogisticMove(map, leftPos, dir.backRight(), dir);
		setLogisticMove(map, rightPos, dir.frontLeft(), dir);
		setLogisticMove(map, rightPos, dir.frontRight(), dir);
		setLogisticMove(map, rightPos, dir.backLeft(), dir);
		setLogisticMove(map, rightPos, dir.backRight(), dir);

		addLogisticWarp(map, leftPos, dir.backLeft(), rightPos, dir.frontLeft());
		addLogisticWarp(map, leftPos, dir.backRight(), rightPos, dir.frontRight());
		addLogisticWarp(map, rightPos, dir.backLeft(), leftPos, dir.frontLeft());
		addLogisticWarp(map, rightPos, dir.backRight(), leftPos, dir.frontRight());
	}

	@Override
	public void populateWorldMap(WorldMap map, DataTable dataTable, BlueprintEntity entity, EntityPrototype prototype) {
		Direction direction = entity.getDirection();
		Point2D.Double belt1Pos = direction.left().offset(entity.getPosition(), 0.5);
		Point2D.Double belt2Pos = direction.right().offset(entity.getPosition(), 0.5);
		map.setBelt(belt1Pos, direction, false, true);
		map.setBelt(belt2Pos, direction, false, true);
	}
}
