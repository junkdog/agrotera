package net.onedaybeard.agrotera.transform;

import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class ClassUtil implements Opcodes
{
	private ClassUtil() {}

	public static void injectMethodStub(ClassWriter cw, String methodName)
	{
		MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, methodName, "()V", null, null);
		mv.visitCode();
		mv.visitInsn(RETURN);
		mv.visitEnd();
	}
	
	public static void writeClass(ClassWriter writer, String file)
	{
//		cr.accept(cw, 0);
//    	PrintWriter printer = new PrintWriter(System.out);
//    	CheckClassAdapter.verify(new ClassReader(writer.toByteArray()), false, printer);
		try (FileOutputStream fos = new FileOutputStream(file))
		{
			fos.write(writer.toByteArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
