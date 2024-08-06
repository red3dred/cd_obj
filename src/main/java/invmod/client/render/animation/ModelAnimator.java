package invmod.client.render.animation;

import invmod.common.util.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.model.ModelPart;

public class ModelAnimator<T extends Enum<T>> {
	private List<Triplet<ModelPart, Integer, List<KeyFrame>>> parts;
	private float animationPeriod;

	public ModelAnimator() {
		this(1.0F);
	}

	public ModelAnimator(float animationPeriod) {
		this.animationPeriod = animationPeriod;
		this.parts = new ArrayList<>(1);
	}

	public ModelAnimator(Map<T, ModelPart> modelParts, Animation<T> animation) {
		this.animationPeriod = animation.getAnimationPeriod();
		this.parts = new ArrayList<>(((Enum[]) animation.getSkeletonType().getEnumConstants()).length);
		for (var entry : modelParts.entrySet()) {
			List<KeyFrame> keyFrames = animation.getKeyFramesFor(entry.getKey());
			if (keyFrames != null) {
				this.parts.add(new Triplet<>(entry.getValue(), Integer.valueOf(0), keyFrames));
			}
		}
	}

	public void addPart(ModelPart part, List<KeyFrame> keyFrames) {
		if (validate(keyFrames)) {
			this.parts.add(new Triplet<>(part, Integer.valueOf(0), keyFrames));
		}
	}

	public void clearParts() {
		this.parts.clear();
	}

	public void updateAnimation(float newTime) {
		for (var entry : this.parts) {
			int prevIndex = entry.getVal2().intValue();
			List<KeyFrame> keyFrames = entry.getVal3();
			KeyFrame prevFrame = keyFrames.get(prevIndex++);
			KeyFrame nextFrame = null;

			if (prevFrame.time() <= newTime) {
				for (; prevIndex < keyFrames.size(); prevIndex++) {
					KeyFrame keyFrame = keyFrames.get(prevIndex);
					if (newTime < keyFrame.time()) {
						nextFrame = keyFrame;
						prevIndex--;
						break;
					}

					prevFrame = keyFrame;
				}

				if (prevIndex >= keyFrames.size()) {
					prevIndex = keyFrames.size() - 1;
					nextFrame = keyFrames.get(0);
				}
			} else {
				for (prevIndex = 0; prevIndex < keyFrames.size(); prevIndex++) {
					KeyFrame keyFrame = keyFrames.get(prevIndex);
					if (newTime < keyFrame.time()) {
						nextFrame = keyFrame;
						prevIndex--;
						prevFrame = keyFrames.get(prevIndex);
						break;
					}
				}
			}
			entry.setVal2(Integer.valueOf(prevIndex));
			interpolate(prevFrame, nextFrame, newTime, entry.getVal1());
		}
	}

	private final Vector3f tempVector = new Vector3f();

	private void interpolate(KeyFrame prevFrame, KeyFrame nextFrame, float time, ModelPart part) {
		if (prevFrame.interpType() == InterpType.LINEAR) {
			float dtPrev = time - prevFrame.time();
			float dtFrame = nextFrame.time() - prevFrame.time();
			if (dtFrame < 0.0F) {
				dtFrame += this.animationPeriod;
			}

			float r = dtPrev / dtFrame;

			Vector3fc rotation = KeyFrame.lerp(r, prevFrame.rotation(), nextFrame.rotation(), tempVector);
			part.pitch = rotation.x();
			part.yaw = rotation.y();
			part.roll = rotation.z();

			if (prevFrame.hasPos()) {
			    Vector3fc pivot = nextFrame.hasPos() ? KeyFrame.lerp(r, prevFrame.pivot(), nextFrame.pivot(), tempVector) : prevFrame.pivot();
			    part.pivotX = pivot.x();
                part.pivotY = pivot.y();
                part.pivotZ = pivot.z();
			}
		}
	}

	private boolean validate(List<KeyFrame> keyFrames) {
		if (keyFrames.size() < 2) {
			return false;
		}
		if (keyFrames.get(0).time() != 0.0F) {
			return false;
		}
		int prevTime = 0;
		for (int i = 1; i < keyFrames.size(); i++) {
			if (keyFrames.get(i).time() <= prevTime) {
				return false;
			}
		}
		return true;
	}
}