package fuzs.armorquickswap.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.event.v1.LoadCompleteCallback;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ClientParticleTypeManager implements PreparableReloadListener {
    public static final ClientParticleTypeManager INSTANCE = new ClientParticleTypeManager();
    private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
    private static final ResourceLocation PARTICLES_ATLAS_INFO = new ResourceLocation("particles");

    private Map<ResourceLocation, ParticleProvider<?>> providers = Maps.newConcurrentMap();
    private Map<ResourceLocation, MutableSpriteSet> spriteSets = Maps.newConcurrentMap();
    private Minecraft minecraft;
    private Boolean registerReloadListener;

    {
        LoadCompleteCallback.EVENT.register(() -> {
            this.providers = ImmutableMap.copyOf(this.providers);
            this.spriteSets = ImmutableMap.copyOf(this.spriteSets);
            this.minecraft = Minecraft.getInstance();
        });
    }

    public void tryRegisterReloadListener(BiConsumer<String, PreparableReloadListener> consumer) {
        if (this.registerReloadListener != null && this.registerReloadListener) {
            consumer.accept("client_particle_types", this);
        }
        this.registerReloadListener = false;
    }

    public <T extends ParticleOptions> void register(ResourceLocation identifier, ParticleProvider<T> particleFactory) {
        if (this.registerReloadListener == null) this.registerReloadListener = true;
        this.providers.put(identifier, particleFactory);
    }

    public <T extends ParticleOptions> void register(ResourceLocation identifier, ParticleProvider.Sprite<T> sprite) {
        if (this.registerReloadListener == null) this.registerReloadListener = true;
        this.register(identifier, (SpriteSet spriteSet) -> {
            return (T particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) -> {
                TextureSheetParticle textureSheetParticle = sprite.createParticle(particleOptions, clientLevel, d, e, f, g, h, i);
                if (textureSheetParticle != null) {
                    textureSheetParticle.pickSprite(spriteSet);
                }

                return textureSheetParticle;
            };
        });
    }

    public <T extends ParticleOptions> void register(ResourceLocation identifier, ParticleEngine.SpriteParticleRegistration<T> particleMetaFactory) {
        if (this.registerReloadListener == null) this.registerReloadListener = true;
        MutableSpriteSet mutableSpriteSet = new MutableSpriteSet();
        this.spriteSets.put(identifier, mutableSpriteSet);
        this.providers.put(identifier, particleMetaFactory.create(mutableSpriteSet));
    }

    @Nullable
    public Particle createParticle(ResourceLocation identifier, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return this.createParticle(identifier, new SimpleParticleType(false), x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Nullable
    public Particle createParticle(ResourceLocation identifier, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        Particle particle = this.makeParticle(identifier, particleData, x, y, z, xSpeed, ySpeed, zSpeed);
        if (particle != null) {
            this.minecraft.particleEngine.add(particle);
            return particle;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends ParticleOptions> Particle makeParticle(ResourceLocation identifier, T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        ParticleProvider<T> particleProvider = (ParticleProvider<T>) this.providers.get(identifier);
        return particleProvider == null ? null : particleProvider.createParticle(particleData, this.minecraft.level, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
        }
        CompletableFuture<List<ParticleDefinition>> completablefuture = CompletableFuture.supplyAsync(() -> {
            return PARTICLE_LISTER.listMatchingResources(resourceManager);
        }, backgroundExecutor).thenCompose((resourceMap) -> {
            List<CompletableFuture<ParticleDefinition>> list = new ArrayList<>(resourceMap.size());
            resourceMap.forEach((p_247903_, p_247904_) -> {
                ResourceLocation resourcelocation = PARTICLE_LISTER.fileToId(p_247903_);
                list.add(CompletableFuture.supplyAsync(() -> {
                    return new ParticleDefinition(resourcelocation, this.loadParticleDescription(resourcelocation, p_247904_));
                }, backgroundExecutor));
            });
            return Util.sequence(list);
        });
        TextureAtlas textureAtlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES, MissingTextureAtlasSprite.getTexture());
        CompletableFuture<SpriteLoader.Preparations> completablefuture1 = SpriteLoader.create(textureAtlas).loadAndStitch(resourceManager, PARTICLES_ATLAS_INFO, 0, backgroundExecutor).thenCompose(SpriteLoader.Preparations::waitForUpload);
        return CompletableFuture.allOf(completablefuture1, completablefuture).thenCompose(preparationBarrier::wait).thenAcceptAsync($ -> {
//                    this.clearParticles();
            reloadProfiler.startTick();
            reloadProfiler.push("upload");
            SpriteLoader.Preparations spriteloader$preparations = completablefuture1.join();
            textureAtlas.upload(spriteloader$preparations);
            reloadProfiler.popPush("bindSpriteSets");
            Set<ResourceLocation> set = new HashSet<>();
            TextureAtlasSprite textureatlassprite = spriteloader$preparations.missing();
            completablefuture.join().forEach((particleDefinition) -> {
                Optional<List<ResourceLocation>> optional = particleDefinition.sprites();
                if (!optional.isEmpty()) {
                    List<TextureAtlasSprite> list = new ArrayList<>();

                    for(ResourceLocation resourcelocation : optional.get()) {
                        TextureAtlasSprite textureatlassprite1 = spriteloader$preparations.regions().get(resourcelocation);
                        if (textureatlassprite1 == null) {
                            set.add(resourcelocation);
                            list.add(textureatlassprite);
                        } else {
                            list.add(textureatlassprite1);
                        }
                    }

                    if (list.isEmpty()) {
                        list.add(textureatlassprite);
                    }

                    this.spriteSets.get(particleDefinition.id()).rebind(list);
                }
            });
            if (!set.isEmpty()) {
                ArmorQuickSwap.LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
            }

            reloadProfiler.pop();
            reloadProfiler.endTick();
        }, gameExecutor);
    }

    private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation registryName, Resource resource) {
        if (!this.spriteSets.containsKey(registryName)) {
//            LOGGER.debug("Redundant texture list for particle: {}", registryName);
            return Optional.empty();
        } else {
            try {
                Reader reader = resource.openAsReader();

                Optional<List<ResourceLocation>> optional;
                try {
                    ParticleDescription particleDescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
                    optional = Optional.of(particleDescription.getTextures());
                } catch (Throwable throwable) {
                    try {
                        reader.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }

                    throw throwable;
                }

                reader.close();

                return optional;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load description for particle " + registryName, e);
            }
        }
    }

    private static class MutableSpriteSet implements SpriteSet {
        private List<TextureAtlasSprite> sprites;

        @Override
        public TextureAtlasSprite get(int age, int lifetime) {
            return this.sprites.get(age * (this.sprites.size() - 1) / lifetime);
        }

        @Override
        public TextureAtlasSprite get(RandomSource random) {
            return this.sprites.get(random.nextInt(this.sprites.size()));
        }

        public void rebind(List<TextureAtlasSprite> sprites) {
            this.sprites = ImmutableList.copyOf(sprites);
        }
    }
}
