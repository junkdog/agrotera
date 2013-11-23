package lombok.eclipse.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;

final class EclipseUtil
{
	private EclipseUtil() {}
	
	static void filterInvalid(List<Object> types)
	{
		for (Iterator<Object> it = types.iterator(); it.hasNext();)
		{
			if (!(it.next() instanceof ClassLiteralAccess))
				it.remove();
		}
	}
}

