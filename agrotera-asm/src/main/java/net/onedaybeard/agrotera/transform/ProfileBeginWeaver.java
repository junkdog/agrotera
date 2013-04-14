package net.onedaybeard.agrotera.transform;

import java.util.List;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

class ProfileBeginWeaver extends AdviceAdapter implements Opcodes
{
	private static final String COMPONENT_ARGUMENT_DESC = "(Ljava/lang/Class;[Ljava/lang/Class;)Lcom/artemis/Aspect;";
	private static final String ASPECT = "com/artemis/Aspect";
	private static final String CLASS = "java/lang/Class";
	
	private ArtemisConfigurationData info;
	
	private boolean aspectIntercepted;
	
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
