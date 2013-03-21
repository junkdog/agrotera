/*
 * Copyright (C) 2010-2012 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import lombok.ProcessingSystem;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import com.artemis.ComponentMapper;

//@ProviderFor(EclipseAnnotationHandler.class)
public class HandleProcessingSystem extends EclipseAnnotationHandler<ProcessingSystem>
{
	@Override
	public void handle(AnnotationValues<ProcessingSystem> annotation, Annotation ast,
		EclipseNode annotationNode)
	{
		annotationNode.addWarning("not shitting you");
		
		List<String> components = annotation.getRawExpressions("requires");
		components.addAll(annotation.getRawExpressions("optional"));
		List<String> systems = annotation.getRawExpressions("systems");
		List<String> managers = annotation.getRawExpressions("managers");
		
		
		
		try
		{
			System.out.println("required: (raw)");
			for (String component : components)
			{
				component = getClassName(component);
				System.out.println("\t" + component);
				injectField(annotationNode.up(), createMapperField(ast, component));
			}
			
			System.out.println("systems:");
			for (String s : systems)
			{
				System.out.println("\t" + s);
			}
			
			
			annotationNode.up().rebuild();
			System.out.println("injected mapper fields");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// TODO: lacking generics
	private FieldDeclaration createMapperField(/*TypeDeclaration klazzType, */ Annotation source, String componentClass)
	{
//		componentClass = componentClass.substring(0, 1).toLowerCase() + componentClass.substring(1);

		String variableName = componentClass.substring(0, 1).toLowerCase() + componentClass.substring(1)
			+ "Mapper";
		
		FieldDeclaration fieldDecl = new FieldDeclaration(variableName.toCharArray(), 0, -1);
		setGeneratedBy(fieldDecl, source);
		fieldDecl.declarationSourceEnd = -1;
		fieldDecl.modifiers = Modifier.PRIVATE;
		fieldDecl.type = createTypeReference("com.artemis.ComponentMapper", fromQualifiedName("net.onedaybeard.rebelescape.component." + componentClass), source);
		fieldDecl.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		int posStart = source.sourceStart, posEnd = source.sourceEnd;
		long pos = (long)posStart << 32 | posEnd;
		
		// set generic type from component
//		TypeReference componentType = new SingleTypeReference(componentClass.toCharArray(), klazz.sourceStart);
		
//		new ParameterizedSingleTypeReference(name, typeArguments, dim, pos)
		
		
		
		
		return fieldDecl;
	}
	
	private MethodDeclaration createInitMethod(TypeDeclaration klazz, List<String> components,
		List<String> systems, List<String> Managers)
	{
		MethodDeclaration method = new MethodDeclaration(klazz.compilationResult);
		
		return method;
	}
	
	private static String getClassName(String klazz)
	{
		return klazz.replaceAll("\\.class$", "");
	}
	
	private static TypeReference createTypeReference(String typeName, char[][] component, Annotation source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		// TODO: hack!
//		typeName = "net.onedaybeard.rebelescape.component." + typeName;
		
		TypeReference typeReference;
		if (typeName.contains(".")) {
			
			char[][] typeNameTokens = fromQualifiedName(typeName);
			long[] pos = new long[typeNameTokens.length];
			Arrays.fill(pos, p);
			
			TypeReference[] genericRefs = new TypeReference[1];
			QualifiedTypeReference genericTypeRef = new QualifiedTypeReference(component, new long[]{0, 0, 0});
			genericRefs[0] = genericTypeRef; 
			
//			typeReference = new QualifiedTypeReference(typeNameTokens, pos);
//			new ParameterizedSingleTypeReference(typeNameTokens, genericRefs, genericTypeRef.dimensions(), pos);
		}
		else {
			typeReference = null;
		}
		
//		setGeneratedBy(typeReference, source);
//		return typeReference;
		return null;
	}
	
	private static TypeReference createTypeReferenceNotGenerics(String typeName, Annotation source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		// TODO: hack!
//		typeName = "net.onedaybeard.rebelescape.component." + typeName;
		
		TypeReference typeReference;
		if (typeName.contains(".")) {
			
			char[][] typeNameTokens = fromQualifiedName(typeName);
			long[] pos = new long[typeNameTokens.length];
			Arrays.fill(pos, p);
			
//			new ParameterizedQualifiedTypeReference(typeNameTokens, typeArguments, dim, positions)
//			new QualifiedTypeReference(sources, poss)
			typeReference = new QualifiedTypeReference(typeNameTokens, pos);
		}
		else {
			typeReference = null;
		}
		
		setGeneratedBy(typeReference, source);
		return typeReference;
	}
}
	
