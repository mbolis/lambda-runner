package it.sorintlab.lambda;

import static com.codepoetics.protonpack.StreamUtils.zip;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

public class Option {
	private final Method method;
	private final List<Param> params;

	public Option(Method method, List<String> paramsSource) {
		this.method = method;

		final Type[] paramTypes = method.getGenericParameterTypes();
		this.params = zip(Stream.of(paramTypes).map(this::paramType), paramsSource.stream(), Param::new) //
				.collect(toList());
	}

	private String paramType(Type type) {
		return type.toString().replace("? super T", "String").replace("? extends R", "String");
	}

	public Method getMethod() {
		return method;
	}

	public List<Param> getParams() {
		return unmodifiableList(params);
	}
}