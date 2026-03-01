@Mixin(LivingEntity.class)
public abstract class EntityMixin {
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            // Проверяем наличие колеса в инвентаре
            for (ItemStack stack : player.getInventory().main) {
                if (stack.getItem() instanceof MahoragaWheel) {
                    MahoragaWheel.handleDamage(player, source, stack);
                    break;
                }
            }
        }
    }
}
