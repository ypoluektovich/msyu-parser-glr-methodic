package org.msyu.parser.methodic;

import java.util.Objects;

final class RepeatRep {

	final Repeat annotation;
	final Object elementRep;

	RepeatRep(Repeat annotation, Object elementRep) {
		this.annotation = annotation;
		this.elementRep = elementRep;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != RepeatRep.class) {
			return false;
		}
		RepeatRep that = (RepeatRep) obj;
		return elementRep.equals(that.elementRep) &&
				(annotation.value() > 0 ?
						annotation.value() == that.annotation.value() :
						that.annotation.value() > 0 &&
								annotation.min() == that.annotation.min() &&
								annotation.max() == that.annotation.max()
				);
	}

	@Override
	public final int hashCode() {
		return annotation.value() > 0 ?
				Objects.hash(elementRep, annotation.value()) :
				Objects.hash(elementRep, annotation.min(), annotation.max());
	}



}
