package invmod.client;

import invmod.client.render.AnimationRegistry;
import invmod.client.render.ModelIMSkeleton;
import invmod.client.render.ModelImp;
import invmod.client.render.ModelThrower;
import invmod.client.render.ModelTrap;
import invmod.client.render.RenderB;
import invmod.client.render.RenderBolt;
import invmod.client.render.RenderBoulder;
import invmod.client.render.RenderBurrower;
import invmod.client.render.RenderEgg;
import invmod.client.render.RenderGiantBird;
import invmod.client.render.RenderIMCreeper;
import invmod.client.render.RenderIMSkeleton;
import invmod.client.render.RenderIMWolf;
import invmod.client.render.RenderIMZombie;
import invmod.client.render.RenderIMZombiePigman;
import invmod.client.render.RenderImp;
import invmod.client.render.RenderInvis;
import invmod.client.render.RenderPigEngy;
import invmod.client.render.RenderSpiderIM;
import invmod.client.render.RenderThrower;
import invmod.client.render.RenderTrap;
import invmod.client.render.animation.Animation;
import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationPhaseInfo;
import invmod.client.render.animation.BonesBirdLegs;
import invmod.client.render.animation.BonesMouth;
import invmod.client.render.animation.BonesWings;
import invmod.client.render.animation.InterpType;
import invmod.client.render.animation.KeyFrame;
import invmod.client.render.animation.Transition;
import invmod.common.ProxyCommon;
import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMBoulder;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMEgg;
import invmod.common.entity.EntityIMGiantBird;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpawnProxy;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMTrap;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.EntitySFX;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class ProxyClient extends ProxyCommon {



	public <T extends Entity> void registerEntityRenderingHandler(Class<T> entityClass, EntityRenderer<T> renderer) {

	}

	public void printGuiMessage(Text message) {
	    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
	}

	@Override
    public void registerEntityRenderers() {
		registerEntityRenderingHandler(EntityIMZombie.class, new RenderIMZombie(new ModelZombie(0, true), 0.5F));
		registerEntityRenderingHandler(EntityIMZombiePigman.class, new RenderIMZombiePigman(new ModelZombie(0, true), 0.5F));
		registerEntityRenderingHandler(EntityIMSkeleton.class, new RenderIMSkeleton(new ModelIMSkeleton(), 0.5F));
		registerEntityRenderingHandler(EntityIMSpider.class, new RenderSpiderIM());
		registerEntityRenderingHandler(EntityIMPigEngy.class, new RenderPigEngy(new ModelBiped(), 0.5F));
		registerEntityRenderingHandler(EntityIMImp.class, new RenderImp(new ModelImp(), 0.3F));
		registerEntityRenderingHandler(EntityIMThrower.class, new RenderThrower(new ModelThrower(), 1.5F));
		registerEntityRenderingHandler(EntityIMBurrower.class, new RenderBurrower());
		registerEntityRenderingHandler(EntityIMWolf.class, new RenderIMWolf());
		registerEntityRenderingHandler(EntityIMBoulder.class, new RenderBoulder());
		registerEntityRenderingHandler(EntityIMTrap.class, new RenderTrap(new ModelTrap()));
		registerEntityRenderingHandler(EntityIMBolt.class, new RenderBolt());
		registerEntityRenderingHandler(EntitySFX.class, new RenderInvis());
		registerEntityRenderingHandler(EntityIMSpawnProxy.class, new RenderInvis());
		registerEntityRenderingHandler(EntityIMEgg.class, new RenderEgg());

		registerEntityRenderingHandler(EntityIMCreeper.class, new RenderIMCreeper());
		registerEntityRenderingHandler(EntityIMBird.class, new RenderB());
		registerEntityRenderingHandler(EntityIMGiantBird.class, new RenderGiantBird());
	}

	@Override
    public void loadAnimations() {
		EnumMap allKeyFrames = new EnumMap(BonesBirdLegs.class);
		List animationPhases = new ArrayList(2);
		int x = 17;
		float totalFrames = 331 + x;

		Map transitions = new HashMap(1);
		Transition defaultTransition = new Transition(AnimationAction.STAND, 1 / totalFrames, 0);
		transitions.put(AnimationAction.STAND, defaultTransition);
		transitions.put(AnimationAction.STAND_TO_RUN, new Transition(AnimationAction.STAND_TO_RUN, 1 / totalFrames, 1 / totalFrames));
		transitions.put(AnimationAction.LEGS_RETRACT, new Transition(AnimationAction.LEGS_RETRACT, 1 / totalFrames, (211 + x) / totalFrames));
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P1, new Transition(AnimationAction.LEGS_CLAW_ATTACK_P1, 1 / totalFrames, (171 + x) / totalFrames));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.STAND, 0, 1 / totalFrames, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.RUN, 38 / totalFrames, 38 / totalFrames);
		transitions.put(AnimationAction.RUN, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.STAND_TO_RUN, 1 / totalFrames, 38 / totalFrames, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.RUN, (170 + x) / totalFrames, 38 / totalFrames);
		transitions.put(AnimationAction.RUN, defaultTransition);
		transitions.put(AnimationAction.STAND, new Transition(AnimationAction.STAND, (170 + x) / totalFrames, 0));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.RUN, 38 / totalFrames, (170 + x) / totalFrames, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.LEGS_UNRETRACT, (251 + x) / totalFrames, (251 + x) / totalFrames);
		transitions.put(AnimationAction.LEGS_UNRETRACT, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_RETRACT, (211 + x) / totalFrames, (251 + x) / totalFrames, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.STAND, (291 + x) / totalFrames, 0);
		transitions.put(AnimationAction.STAND, defaultTransition);
		transitions.put(AnimationAction.LEGS_RETRACT, new Transition(AnimationAction.LEGS_RETRACT, (291 + x) / totalFrames, (211 + x) / totalFrames));
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P1, new Transition(AnimationAction.LEGS_CLAW_ATTACK_P1, (291 + x) / totalFrames, (291 + x) / totalFrames));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_UNRETRACT, (251 + x) / totalFrames, (291 + x) / totalFrames, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.LEGS_CLAW_ATTACK_P2, (331 + x) / totalFrames, (171 + x) / totalFrames);
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P2, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_CLAW_ATTACK_P1, (291 + x) / totalFrames, (331 + x) / totalFrames, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.STAND, (211 + x) / totalFrames, 0);
		transitions.put(AnimationAction.STAND, defaultTransition);
		transitions.put(AnimationAction.LEGS_RETRACT, new Transition(AnimationAction.LEGS_RETRACT, (211 + x) / totalFrames, (211 + x) / totalFrames));
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P1, new Transition(AnimationAction.LEGS_CLAW_ATTACK_P1, (211 + x) / totalFrames, (291 + x) / totalFrames));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_CLAW_ATTACK_P2, (171 + x) / totalFrames, (211 + x) / totalFrames, defaultTransition, transitions));

		float frameUnit = 1 / totalFrames;
		float runBegin = 38 * frameUnit;
		float runEnd = (170 + x) * frameUnit;

		List leftThighFrames = new ArrayList(13);
		leftThighFrames.add(new KeyFrame(0, -15, 0, -5, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(1 * frameUnit, -15, 0, -5, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(5 * frameUnit, -12.6F, 0.2F, 5, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(10 * frameUnit, 21.200001F, -0.6F, 5.2F, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(15 * frameUnit, -32, -1.7F, 5.7F, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(25 * frameUnit, -57, -6.4F, 9, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(35 * frameUnit, -76.5F, -19.299999F, 21.200001F, InterpType.LINEAR));
		KeyFrame.toRadians(leftThighFrames);

		List leftThighRunCycle = new ArrayList(7);
		leftThighRunCycle.add(new KeyFrame(38 * frameUnit, -74.099998F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame(44 * frameUnit, -63.700001F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((80 + x) * frameUnit, 13.1F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((101 + x) * frameUnit, 35.700001F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((110 + x) * frameUnit, 20, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((140 + x) * frameUnit, -33, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((170 + x) * frameUnit, -74.099998F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((171 + x) * frameUnit, -76, 0, -5.6F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((211 + x) * frameUnit, -15, 0, -5, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((251 + x) * frameUnit, 9, 0, 0, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((291 + x) * frameUnit, -15, 0, -5, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((331 + x) * frameUnit, -76, 0, -5.6F, InterpType.LINEAR));
		KeyFrame.toRadians(leftThighRunCycle);

		List rightThighFrames = new ArrayList(13);
		rightThighFrames.add(new KeyFrame(0, -15, 0, 0, InterpType.LINEAR));
		rightThighFrames.add(new KeyFrame(1 * frameUnit, -15, 0, 0, InterpType.LINEAR));
		rightThighFrames.add(new KeyFrame(37 * frameUnit, -15, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(rightThighFrames);
		List rightThighRunCycle = KeyFrame.cloneFrames(leftThighRunCycle);
		KeyFrame.mirrorFramesX(rightThighRunCycle);
		KeyFrame.offsetFramesCircular(rightThighRunCycle, runBegin, runEnd, (runEnd - runBegin) / 2);

		leftThighFrames.addAll(leftThighRunCycle);
		rightThighFrames.addAll(rightThighRunCycle);
		allKeyFrames.put(BonesBirdLegs.LEFT_KNEE, leftThighFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_KNEE, rightThighFrames);

		List leftLegFrames = new ArrayList(19);
		leftLegFrames.add(new KeyFrame(0, -41, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(1 * frameUnit, -41, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(10 * frameUnit, -80.300003F, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(25 * frameUnit, -44.200001F, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(35 * frameUnit, -5.6F, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftLegFrames);


		List leftLegRunCycle = new ArrayList(16);
		leftLegRunCycle.add(new KeyFrame(38 * frameUnit, 6.6F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(44 * frameUnit, 6.5F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(47 * frameUnit, -11, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(50 * frameUnit, -24, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(53 * frameUnit, -32.900002F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(56 * frameUnit, -40.799999F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(59 * frameUnit, -46.700001F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(62 * frameUnit, -45.799999F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(82 * frameUnit, -45.599998F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(97 * frameUnit, -17.1F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((85 + x) * frameUnit, 0.75F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((90 + x) * frameUnit, -0.4F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((101 + x) * frameUnit, -43, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((115 + x) * frameUnit, -60.099998F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((154 + x) * frameUnit, -50.5F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((170 + x) * frameUnit, 6.6F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((171 + x) * frameUnit, -37, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((211 + x) * frameUnit, -41, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((251 + x) * frameUnit, 15, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((291 + x) * frameUnit, -41, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((331 + x) * frameUnit, -37, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftLegRunCycle);

		List rightLegFrames = new ArrayList(19);
		rightLegFrames.add(new KeyFrame(0, -41, 0, 0, InterpType.LINEAR));
		rightLegFrames.add(new KeyFrame(37 * frameUnit, -41, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(rightLegFrames);

		List rightLegRunCycle = KeyFrame.cloneFrames(leftLegRunCycle);
		KeyFrame.mirrorFramesX(rightLegRunCycle);
		KeyFrame.offsetFramesCircular(rightLegRunCycle, runBegin, runEnd, (runEnd - runBegin) / 2);

		leftLegFrames.addAll(leftLegRunCycle);
		rightLegFrames.addAll(rightLegRunCycle);
		allKeyFrames.put(BonesBirdLegs.LEFT_ANKLE, leftLegFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_ANKLE, rightLegFrames);

		List leftAnkleFrames = new ArrayList(27);
		leftAnkleFrames.add(new KeyFrame(0, -0.4F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(1 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(5 * frameUnit, 31.700001F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(10 * frameUnit, 45, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(20 * frameUnit, 52.799999F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(25 * frameUnit, 51.599998F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(30 * frameUnit, 42.299999F, -5, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftAnkleFrames);
		List leftAnkleRunCycle = new ArrayList(21);
		leftAnkleRunCycle.add(new KeyFrame(38 * frameUnit, 28.799999F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(44 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(47 * frameUnit, 7.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(50 * frameUnit, 12.4F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(53 * frameUnit, 12.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(56 * frameUnit, 11.8F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(59 * frameUnit, 8.5F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(62 * frameUnit, 1.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(82 * frameUnit, -1, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(87 * frameUnit, -5.5F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(90 * frameUnit, -0.7F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(93 * frameUnit, 6.8F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(97 * frameUnit, -4.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((85 + x) * frameUnit, 20.700001F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((95 + x) * frameUnit, 34.200001F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((100 + x) * frameUnit, 45.599998F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((110 + x) * frameUnit, 36.599998F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((115 + x) * frameUnit, 38.400002F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((124 + x) * frameUnit, 50, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((140 + x) * frameUnit, 45.299999F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((154 + x) * frameUnit, 52.900002F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((170 + x) * frameUnit, 25, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((171 + x) * frameUnit, -38, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((211 + x) * frameUnit, 0, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((251 + x) * frameUnit, 22, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((291 + x) * frameUnit, 0, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((331 + x) * frameUnit, -38, -5, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftAnkleRunCycle);

		List rightAnkleFrames = new ArrayList(27);
		rightAnkleFrames.add(new KeyFrame(0, -0.4F, -5, 0, InterpType.LINEAR));
		rightAnkleFrames.add(new KeyFrame(1 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		rightAnkleFrames.add(new KeyFrame(37 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		KeyFrame.toRadians(rightAnkleFrames);
		List rightAnkleRunCycle = KeyFrame.cloneFrames(leftAnkleRunCycle);
		KeyFrame.mirrorFramesX(rightAnkleRunCycle);
		KeyFrame.offsetFramesCircular(rightAnkleRunCycle, runBegin, runEnd, (runEnd - runBegin) / 2);

		leftAnkleFrames.addAll(leftAnkleRunCycle);
		rightAnkleFrames.addAll(rightAnkleRunCycle);
		allKeyFrames.put(BonesBirdLegs.LEFT_METATARSOPHALANGEAL_ARTICULATIONS, leftAnkleFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_METATARSOPHALANGEAL_ARTICULATIONS, rightAnkleFrames);

		List leftBackClawFrames = new ArrayList(21);
		leftBackClawFrames.add(new KeyFrame(0, 77, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((170 + x) * frameUnit, 77, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((171 + x) * frameUnit, 84, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((211 + x) * frameUnit, 77, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((251 + x) * frameUnit, -7.5F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((291 + x) * frameUnit, 77, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((331 + x) * frameUnit, 84, 0, 0, InterpType.LINEAR));

		KeyFrame.toRadians(leftBackClawFrames);
		List rightBackClawFrames = KeyFrame.cloneFrames(leftBackClawFrames);
		KeyFrame.mirrorFramesX(rightBackClawFrames);

		allKeyFrames.put(BonesBirdLegs.LEFT_BACK_CLAW, leftBackClawFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_BACK_CLAW, rightBackClawFrames);

		Animation birdRun = new Animation(BonesBirdLegs.class, 1, 0.04651163F, allKeyFrames, animationPhases);
		AnimationRegistry.instance().registerAnimation("bird_run", birdRun);

		EnumMap allKeyFramesWings = new EnumMap(BonesWings.class);
		animationPhases = new ArrayList(3);

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.WINGFLAP, 0.2714932F, 0);
		transitions.put(AnimationAction.WINGFLAP, defaultTransition);
		transitions.put(AnimationAction.WINGTUCK, new Transition(AnimationAction.WINGTUCK, 0.06787331F, 0.2760181F));
		transitions.put(AnimationAction.WINGGLIDE, new Transition(AnimationAction.WINGGLIDE, 0.06787331F, 0.8190045F));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGFLAP, 0, 0.2714932F, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.WINGSPREAD, 0.5429865F, 0.5475113F);
		transitions.put(AnimationAction.WINGSPREAD, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGTUCK, 0.2760181F, 0.5429865F, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.WINGTUCK, 0.8190045F, 0.2760181F);
		transitions.put(AnimationAction.WINGTUCK, defaultTransition);
		transitions.put(AnimationAction.WINGFLAP, new Transition(AnimationAction.WINGFLAP, 0.8190045F, 0.06787331F));
		transitions.put(AnimationAction.WINGGLIDE, new Transition(AnimationAction.WINGGLIDE, 0.8190045F, 0.8190045F));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGSPREAD, 0.5475113F, 0.8190045F, defaultTransition, transitions));

		transitions = new HashMap(1);
		defaultTransition = new Transition(AnimationAction.WINGGLIDE, 1, 0.8190045F);
		transitions.put(AnimationAction.WINGGLIDE, defaultTransition);
		transitions.put(AnimationAction.WINGFLAP, new Transition(AnimationAction.WINGFLAP, 1, 0.06787331F));
		transitions.put(AnimationAction.WINGTUCK, new Transition(AnimationAction.WINGTUCK, 1, 0.2760181F));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGGLIDE, 0.8190045F, 1, defaultTransition, transitions));

		frameUnit = 0.004524887F;
		List<KeyFrame> rightInnerWingFrames = KeyFrame.toRadians(List.of(
		        new KeyFrame(0, 2, -48, 0, 7, -8, 6, InterpType.LINEAR),
		        new KeyFrame(5 * frameUnit, 4, -38, 0, InterpType.LINEAR),
		        new KeyFrame(10 * frameUnit, 5.5F, -27.5F, 0, InterpType.LINEAR),
		        new KeyFrame(15 * frameUnit, 5.5F, -7, 0, InterpType.LINEAR),
		        new KeyFrame(20 * frameUnit, 5.5F, 15, 0, InterpType.LINEAR),
		        new KeyFrame(25 * frameUnit, 4.5F, 30, 0, InterpType.LINEAR),
		        new KeyFrame(30 * frameUnit, 2, 38, 9, InterpType.LINEAR),
		        new KeyFrame(35 * frameUnit, 1, 20, 0, InterpType.LINEAR),
		        new KeyFrame(40 * frameUnit, 1, 3.5F, 0, InterpType.LINEAR),
		        new KeyFrame(45 * frameUnit, 1, -19, 0, InterpType.LINEAR),
		        new KeyFrame(50 * frameUnit, -3, -38, 0, InterpType.LINEAR),
		        new KeyFrame(55 * frameUnit, -1, -48, 0, InterpType.LINEAR),
		        new KeyFrame(60 * frameUnit, 2, -48, 0, InterpType.LINEAR),
		        new KeyFrame(61 * frameUnit, 5.5F, -7, 0, 7, -8, 6, InterpType.LINEAR),
		        new KeyFrame(121 * frameUnit, 0.71F, 88.599998F, 0, 11, -8, 9, InterpType.LINEAR),
		        new KeyFrame(181 * frameUnit, 5.5F, -7, 0, 7, -8, 6, InterpType.LINEAR),
		        new KeyFrame(209 * frameUnit, 5.5F, -5, 0, InterpType.LINEAR),
		        new KeyFrame(221 * frameUnit, 5.5F, -7, 0, InterpType.LINEAR)));
		List<KeyFrame> rightOuterWingFrames = KeyFrame.toRadians(List.of(
		        new KeyFrame(0, 2, 34.5F, 0, 23, 1, 0, InterpType.LINEAR),
		        new KeyFrame(5 * frameUnit, 5, 13, -7, InterpType.LINEAR),
		        new KeyFrame(10 * frameUnit, 7, 8.5F, -10, InterpType.LINEAR),
		        new KeyFrame(15 * frameUnit, 7.5F, -2.5F, -10, InterpType.LINEAR),
		        new KeyFrame(25 * frameUnit, 5, 7, -10, InterpType.LINEAR),
		        new KeyFrame(30 * frameUnit, 2, 15, 0, InterpType.LINEAR),
		        new KeyFrame(35 * frameUnit, -3, 37, 12, InterpType.LINEAR),
		        new KeyFrame(40 * frameUnit, -9, 56, 27, InterpType.LINEAR),
		        new KeyFrame(45 * frameUnit, -13, 68, 28, InterpType.LINEAR),
		        new KeyFrame(50 * frameUnit, -13.5F, 70, 31.5F, InterpType.LINEAR),
		        new KeyFrame(53 * frameUnit, -9, 71, 31, InterpType.LINEAR),
		        new KeyFrame(55 * frameUnit, -3.5F, 65.5F, 22, InterpType.LINEAR),
		        new KeyFrame(58 * frameUnit, 0, 52, 8, InterpType.LINEAR),
		        new KeyFrame(60 * frameUnit, 2, 34.5F, 0, InterpType.LINEAR),
		        new KeyFrame(61 * frameUnit, -5, -2.5F, -10, 23, 1, 0, InterpType.LINEAR),
		        new KeyFrame(76 * frameUnit, 0, 0, 15, 22, 1, 0, InterpType.LINEAR),
		        new KeyFrame(101 * frameUnit, 0, 0, 83, 20.33F, 1, 0, InterpType.LINEAR),
		        new KeyFrame(121 * frameUnit, 0, 0, 90, 19, 1, 0, InterpType.LINEAR),
		        new KeyFrame(141 * frameUnit, 0, 0, 83, 20.33F, 1, 0, InterpType.LINEAR),
		        new KeyFrame(166 * frameUnit, 0, 0, 15, 22, 1, 0, InterpType.LINEAR),
                new KeyFrame(181 * frameUnit, -5, -2.5F, -10, 23, 1, 0, InterpType.LINEAR),
                new KeyFrame(209 * frameUnit, -5, -1.3F, -10, InterpType.LINEAR),
                new KeyFrame(221 * frameUnit, -5, -2.5F, -10, InterpType.LINEAR)));

		allKeyFramesWings.put(BonesWings.LEFT_SHOULDER, rightInnerWingFrames);
        allKeyFramesWings.put(BonesWings.RIGHT_SHOULDER, KeyFrame.mirrorFramesX(rightInnerWingFrames));
		allKeyFramesWings.put(BonesWings.LEFT_ELBOW, rightOuterWingFrames);
		allKeyFramesWings.put(BonesWings.RIGHT_ELBOW, KeyFrame.mirrorFramesX(rightOuterWingFrames));

		Animation wingFlap = new Animation(BonesWings.class, 1, 0.01666667F, allKeyFramesWings, animationPhases);
		AnimationRegistry.instance().registerAnimation("wing_flap_2_piece", wingFlap);

		float _frameUnit = 0.008333334F;
		Transition mouthOpen = new Transition(AnimationAction.MOUTH_OPEN, 1, 0);
		Transition mouthClose = new Transition(AnimationAction.MOUTH_CLOSE, 0.5F, 0.5083333F);

		AnimationRegistry.instance().registerAnimation("bird_beak", new Animation(BonesMouth.class, 1, 0.1F, new EnumMap<>(Map.of(
                BonesMouth.UPPER_MOUTH, KeyFrame.toRadians(List.of(
                        new KeyFrame(0 * _frameUnit, 0, 0, 0, InterpType.LINEAR),
                        new KeyFrame(60 * _frameUnit, -8, 0, 0, InterpType.LINEAR),
                        new KeyFrame(120 * _frameUnit, 0, 0, 0, InterpType.LINEAR))),
                BonesMouth.LOWER_MOUTH, KeyFrame.toRadians(List.of(
                        new KeyFrame(0 * _frameUnit, 0, 0, 0, InterpType.LINEAR),
                        new KeyFrame(60 * _frameUnit, 20, 0, 0, InterpType.LINEAR),
                        new KeyFrame(120 * _frameUnit, 0, 0, 0, InterpType.LINEAR))))), List.of(
                new AnimationPhaseInfo(AnimationAction.MOUTH_OPEN, 0, 0.5F, mouthClose, Map.of(AnimationAction.MOUTH_CLOSE, mouthClose)),
                new AnimationPhaseInfo(AnimationAction.MOUTH_CLOSE, 0.5F, 1, mouthOpen, Map.of(AnimationAction.MOUTH_OPEN, mouthOpen)))
        ));
	}

	@Override
    public File getFile(String fileName) {
		return new File(FMLClientHandler.instance().getClient().mcDataDir.getPath() + fileName);
	}
}