package com.pau101.fairylights.server.jingle;

import com.google.common.collect.Sets;
import com.pau101.fairylights.server.fastener.connection.type.hanginglights.Light;
import com.pau101.fairylights.server.jingle.Jingle.PlayTick;
import com.pau101.fairylights.server.sound.FLSounds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JinglePlayer {
	private static final Set<String> WITH_LOVE = Sets.newHashSet("my_anthem", "im_fine_thank_you");

	private State<?> state = new NotPlayingState();

	@Nullable
	public Jingle getJingle() {
		return state.getJingle();
	}

	public boolean isPlaying() {
		return state.isPlaying();
	}

	public float getProgress() {
		return state.getProgress();
	}

	public void play(JingleLibrary library, Jingle jingle, int lightOffset) {
		state = new PlayingState(library, jingle, lightOffset);
	}

	public void tick(World world, Vec3d origin, Light[] lights, boolean isClient) {
		state = state.tick(world, origin, lights, isClient);
	}

	public NBTTagCompound serialize() {
		return StateType.serialize(state);
	}

	public void deserialize(NBTTagCompound compound) {
		state = StateType.deserialize(compound);
	}

	private enum StateType {
		NOT_PLAYING(NotPlayingState.FACTORY),
		PLAYING(PlayingState.FACTORY);

		private final static Map<String, StateType> MAP = Stream.of(values())
				.collect(Collectors.toMap(StateType::getId, Function.identity()));

		private final StateFactory<?> factory;

		StateType(StateFactory<?> factory) {
			this.factory = factory;
		}

		private String getId() {
			return factory.getId();
		}

		private StateFactory<?> getFactory() {
			return factory;
		}

		public static <S extends State<S>> NBTTagCompound serialize(State<S> state) {
			StateFactory<S> factory = state.getFactory();
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("state", factory.getId());
			compound.setTag("data", factory.serialize(state.resolve()));
			return compound;
		}

		public static State<?> deserialize(NBTTagCompound compound) {
			return MAP.getOrDefault(compound.getString("state"), NOT_PLAYING)
					.getFactory()
					.deserialize(compound.getCompoundTag("data"));
		}
	}

	private static abstract class StateFactory<S extends State<S>> {
		public abstract String getId();

		public abstract NBTTagCompound serialize(S state);

		public abstract State<?> deserialize(NBTTagCompound compound);
	}

	private static abstract class State<S extends State<S>> {
		public abstract Jingle getJingle();

		public abstract boolean isPlaying();

		public abstract float getProgress();

		public abstract State<?> tick(World world, Vec3d origin, Light[] lights, boolean isClient);

		public abstract StateFactory<S> getFactory();

		public abstract S resolve();
	}

	private static final class NotPlayingState extends State<NotPlayingState> {
		public static final StateFactory<NotPlayingState> FACTORY = newFactory();

		@Override
		public Jingle getJingle() {
			return null;
		}

		@Override
		public boolean isPlaying() {
			return false;
		}

		@Override
		public float getProgress() {
			return 0;
		}

		@Override
		public State<?> tick(World world, Vec3d origin, Light[] lights, boolean isClient) {
			return this;
		}

		@Override
		public StateFactory<NotPlayingState> getFactory() {
			return FACTORY;
		}

		@Override
		public NotPlayingState resolve() {
			return this;
		}

		private static StateFactory<NotPlayingState> newFactory() {
			return new StateFactory<NotPlayingState>() {
				@Override
				public String getId() {
					return "not_playing";
				}

				@Override
				public NBTTagCompound serialize(NotPlayingState state) {
					return new NBTTagCompound();
				}

				@Override
				public NotPlayingState deserialize(NBTTagCompound compound) {
					return new NotPlayingState();
				}
			};
		}
	}

	private static final class PlayingState extends State<PlayingState> {
		public static final StateFactory<PlayingState> FACTORY = newFactory();

		private final JingleLibrary library;

		private final Jingle jingle;

		private final int lightOffset;

		private final List<PlayTick> playTicks;

		private final int length;

		private final EnumParticleTypes[] noteParticle;

		private int index;

		private int rest;

		private int time;

		private PlayingState(JingleLibrary library, Jingle jingle, int lightOffset) {
			this(library, jingle, lightOffset, jingle.getPlayTicks(), jingle.getLength(), getParticles(jingle));
		}

		private PlayingState(JingleLibrary library, Jingle jingle, int lightOffset, List<PlayTick> playTicks, int length, EnumParticleTypes[] noteParticle) {
			this.library = library;
			this.jingle = jingle;
			this.lightOffset = lightOffset;
			this.playTicks = playTicks;
			this.length = length;
			this.noteParticle = noteParticle;
		}

		@Override
		public Jingle getJingle() {
			return jingle;
		}

		@Override
		public boolean isPlaying() {
			return true;
		}

		@Override
		public float getProgress() {
			return time / (float) length;
		}

		@Override
		public State<?> tick(World world, Vec3d origin, Light[] lights, boolean isClient) {
			time++;
			if (rest <= 0) {
				if (index >= playTicks.size()) {
					return new NotPlayingState();
				}
				PlayTick playTick = playTicks.get(index++);
				rest = playTick.getLength() - 1;
				if (isClient) {
					play(world, origin, lights, playTick);
				}
			} else {
				rest--;
			}
			return this;
		}

		private void play(World world, Vec3d origin, Light[] lights, PlayTick playTick) {
			for (int note : playTick.getNotes()) {
				int idx = note - jingle.getLowestNote() + lightOffset;
				if (idx >= 0 && idx < lights.length) {
					lights[idx].jingle(world, origin, note, FLSounds.JINGLE_BELL, noteParticle);
				}
			}
		}

		@Override
		public StateFactory<PlayingState> getFactory() {
			return FACTORY;
		}

		@Override
		public PlayingState resolve() {
			return this;
		}

		private static StateFactory<PlayingState> newFactory() {
			return new StateFactory<PlayingState>() {
				@Override
				public String getId() {
					return "playing";
				}

				@Override
				public NBTTagCompound serialize(PlayingState state) {
					NBTTagCompound compound = new NBTTagCompound();
					compound.setInteger("library", state.library.getId());
					compound.setString("jingle", state.jingle.getId());
					compound.setInteger("lightOffset", state.lightOffset);
					compound.setInteger("index", state.index);
					compound.setInteger("rest", state.rest);
					compound.setInteger("time", state.time);
					return compound;
				}

				@Override
				public State<?> deserialize(NBTTagCompound compound) {
					JingleLibrary library = JingleLibrary.fromId(compound.getInteger("library"));
					Jingle jingle = library.get(compound.getString("jingle"));
					if (jingle == null) {
						return new NotPlayingState();
					}
					int lightOffset = compound.getInteger("lightOffset");
					PlayingState state = new PlayingState(library, jingle, lightOffset);
					state.index = compound.getInteger("index");
					state.rest = compound.getInteger("rest");
					state.time = compound.getInteger("time");
					return state;
				}
			};
		}

		private static EnumParticleTypes[] getParticles(Jingle jingle) {
			if ("playing_with_fire".equals(jingle.getId())) {
				return new EnumParticleTypes[] { EnumParticleTypes.NOTE, EnumParticleTypes.LAVA };
			}
			if (WITH_LOVE.contains(jingle.getId())) {
				return new EnumParticleTypes[] { EnumParticleTypes.NOTE, EnumParticleTypes.HEART };
			}
			return new EnumParticleTypes[] { EnumParticleTypes.NOTE };
		}
	}
}
