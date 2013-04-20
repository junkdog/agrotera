package net.onedaybeard.agrotera.transform;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

class ProfileBeginWeaver extends AdviceAdapter implements Opcodes
{
	private ArtemisConfigurationData info;
	
	ProfileBeginWeaver(MethodVisitor methodVisitor, ArtemisConfigurationData info, int access, String name, String desc)
	{
		super(Opcodes.ASM4, methodVisitor, access, name, desc);
		this.info = info;
	}
	
	@Override
	protected void onMethodEnter()
	{
		String systemName = info.current.getInternalName();
		String profiler = info.profilerClass.getInternalName();
		String profileDescriptor = info.profilerClass.getDescriptor();
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, systemName, "$profiler", profileDescriptor);
		mv.visitMethodInsn(INVOKEVIRTUAL, profiler, "start", "()V");
	}
}
