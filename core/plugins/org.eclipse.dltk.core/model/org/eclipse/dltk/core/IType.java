/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.core;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IType extends IMember {

	String[] getSuperClasses() throws ModelException;
	/**
	 * Returns the field with the specified name
	 * in this type (for example, <code>"bar"</code>).
	 * This is a handle-only method.  The field may or may not exist.
	 * 
	 * @param name the given name
	 * @return the field with the specified name in this type
	 */
	IField getField(String name);
	
	/**
	 * Returns the fields declared by this type.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source, otherwise, the results are
	 * in no particular order.  For binary types, this includes synthetic fields.
	 *
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the fields declared by this type
	 */
	IField[] getFields() throws ModelException;
	/**
	 * Returns the member type declared in this type with the given simple name.
	 * This is a handle-only method. The type may or may not exist.
	 * 
	 * @param name the given simple name
	 * @return the member type declared in this type with the given simple name
	 */
	IType getType(String name);
	/**
	 * Returns the immediate member types declared by this type.
	 * The results are listed in the order in which they appear in the source or class file.
	 *
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the immediate member types declared by this type
	 */
	IType[] getTypes() throws ModelException;
	/**
	 * Returns the method with the specified name and parameter types
	 * in this type (for example, <code>"foo", {"I", "QString;"}</code>).
	 * To get the handle for a constructor, the name specified must be the
	 * simple name of the enclosing type.
	 * This is a handle-only method.  The method may or may not be present.
	 * <p>
	 * The type signatures may be either unresolved (for source types)
	 * or resolved (for binary types), and either basic (for basic types)
	 * or rich (for parameterized types).
	 * </p>
	 * 
	 * @param name the given name
	 * @return the method with the specified name and parameter types in this type
	 */
	IMethod getMethod(String name);
	
	/**
	 * Returns the methods and constructors declared by this type.
	 * For binary types, this may include the special <code>&lt;clinit&gt;</code>; method 
	 * and synthetic methods.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source, otherwise, the results are
	 * in no particular order.
	 *
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the methods and constructors declared by this type
	 */
	IMethod[] getMethods() throws ModelException;
	
	/**
	 * Returns the fully qualified name of this type, 
	 * including qualification for any containing types and packages.
	 * This is the name of the package, followed by <code>'.'</code>,
	 * followed by the type-qualified name using the <code>enclosingTypeSeparator</code>.
	 * This is a handle-only method.
	 *
	 * @param enclosingTypeSeparator the given enclosing type separator
	 * @return the fully qualified name of this type, including qualification for any containing types and packages
	 * @see IType#getTypeQualifiedName(char)
	 */
	String getFullyQualifiedName(String enclosingTypeSeparator);
	
	/**
	 * @return
	 */
	String getFullyQualifiedName();
	
	/**
	 * Do code completion inside a code snippet in the context of the current type.
	 * 
	 * If the type can access to his source code and the insertion position is valid,
	 * then completion is performed against source. Otherwise the completion is performed
	 * against type structure and given locals variables.
	 * 
	 * @param snippet the code snippet
	 * @param insertion the position with in source where the snippet
	 * is inserted. This position must not be in comments.
	 * A possible value is -1, if the position is not known.
	 * @param position the position within snippet where the user 
	 * is performing code assist.
	 * @param localVariableTypeNames an array (possibly empty) of fully qualified 
	 * type names of local variables visible at the current scope
	 * @param localVariableNames an array (possibly empty) of local variable names 
	 * that are visible at the current scope
	 * @param localVariableModifiers an array (possible empty) of modifiers for 
	 * local variables
	 * @param isStatic whether the current scope is in a static context
	 * @param requestor the completion requestor
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 *
	 */
	void codeComplete(
		char[] snippet,
		int insertion,
		int position,
		char[][] localVariableTypeNames,
		char[][] localVariableNames,
		int[] localVariableModifiers,
		boolean isStatic,
		CompletionRequestor requestor)
		throws ModelException;

	/**
	 * Do code completion inside a code snippet in the context of the current type.
	 * It considers types in the working copies with the given owner first. In other words, 
	 * the owner's working copies will take precedence over their original compilation units
	 * in the workspace.
	 * <p>
	 * Note that if a working copy is empty, it will be as if the original compilation
	 * unit had been deleted.
	 * </p><p>
	 * If the type can access to his source code and the insertion position is valid,
	 * then completion is performed against source. Otherwise the completion is performed
	 * against type structure and given locals variables.
	 * </p>
	 * 
	 * @param snippet the code snippet
	 * @param insertion the position with in source where the snippet
	 * is inserted. This position must not be in comments.
	 * A possible value is -1, if the position is not known.
	 * @param position the position with in snippet where the user 
	 * is performing code assist.
	 * @param localVariableTypeNames an array (possibly empty) of fully qualified 
	 * type names of local variables visible at the current scope
	 * @param localVariableNames an array (possibly empty) of local variable names 
	 * that are visible at the current scope
	 * @param localVariableModifiers an array (possible empty) of modifiers for 
	 * local variables
	 * @param isStatic whether the current scope is in a static context
	 * @param requestor the completion requestor
	 * @param owner the owner of working copies that take precedence over their original compilation units
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 *
	 */
	void codeComplete(
		char[] snippet,
		int insertion,
		int position,
		char[][] localVariableTypeNames,
		char[][] localVariableNames,
		int[] localVariableModifiers,
		boolean isStatic,
		CompletionRequestor requestor,
		WorkingCopyOwner owner)
		throws ModelException;
	
	/**
	 * Returns the package fragment in which this element is defined.
	 * This is a handle-only method.
	 * 
	 * @return the package fragment in which this element is defined
	 */
	IScriptFolder getScriptFolder();

	/**
	 * Returns the type-qualified name of this type, 
	 * including qualification for any enclosing types,
	 * but not including package qualification.
	 * For source types, this consists of the simple names of any enclosing types, 
	 * separated by <code>'$'</code>, followed by the simple name of this type
	 * or the occurence count of this type if it is anonymous.
	 * For binary types, this is the name of the class file without the ".class" suffix.
	 * This is a handle-only method.
	 * 
	 * @return the type-qualified name of this type
	 */
	String getTypeQualifiedName();
	
	/**
	 * Returns the type-qualified name of this type, 
	 * including qualification for any enclosing types,
	 * but not including package qualification.
	 * For source types, this consists of the simple names of any enclosing types, 
	 * separated by <code>enclosingTypeSeparator</code>, followed by the 
	 * simple name of this type  or the occurence count of this type if it is anonymous.
	 * For binary types, this is the name of the class file without the ".class" suffix.
	 * 
	 * For example:
	 * <ul>
	 * <li>the type qualified name of a class B defined as a member of a class A
	 *     using the '.' separator is "A.B"</li>
	 * <li>the type qualified name of a class B defined as a member of a class A
	 *     using the '$' separator is "A$B"</li>
	 * <li>the type qualified name of a binary type whose class file is A$B.class
	 *     using the '.' separator is "A$B"</li>
	 * <li>the type qualified name of a binary type whose class file is A$B.class
	 *     using the '$' separator is "A$B"</li>
	 * <li>the type qualified name of an anonymous binary type whose class file is A$1.class
	 *     using the '.' separator is "A$1"</li>
	 * </ul>
	 *
	 * This is a handle-only method.
	 * 
	 * @param enclosingTypeSeparator the specified enclosing type separator
	 * @return the type-qualified name of this type
	 *
	 */
	String getTypeQualifiedName(String enclosingTypeSeparator);
	
	/** 
	 * Finds the methods in this type that correspond to
	 * the given method.
	 * A method m1 corresponds to another method m2 if:
	 * <ul>
	 * <li>m1 has the same element name as m2.
	 * <li>m1 has the same number of arguments as m2 and
	 *     the simple names of the argument types must be equals.
	 * <li>m1 exists.
	 * </ul>
	 * @param method the given method
	 * @return the found method or <code>null</code> if no such methods can be found.
	 * 
	 *
	 */
	IMethod[] findMethods(IMethod method);
	
	
	// Type hierarchies.
	/**
	 * Loads a previously saved ITypeHierarchy from an input stream. A type hierarchy can
	 * be stored using ITypeHierachy#store(OutputStream).
	 * 
	 * Only hierarchies originally created by the following methods can be loaded:
	 * <ul>
	 * <li>IType#newSupertypeHierarchy(IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(IJavaProject, IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(IProgressMonitor)</li>
	 * </ul>
	 * 
	 * @param input stream where hierarchy will be read
	 * @param monitor the given progress monitor
	 * @return the stored hierarchy
	 * @exception ModelException if the hierarchy could not be restored, reasons include:
	 *      - type is not the focus of the hierarchy or 
	 *		- unable to read the input stream (wrong format, IOException during reading, ...)
	 * @see ITypeHierarchy#store(java.io.OutputStream, IProgressMonitor)
	 */
	ITypeHierarchy loadTypeHierachy(InputStream input, IProgressMonitor monitor) throws ModelException;
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes.
	 *
	 * @param monitor the given progress monitor
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return a type hierarchy for this type containing this type and all of its supertypes
	 */
	ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws ModelException;
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes, considering types in the given 
	 * working copies. In other words, the list of working copies will take 
	 * precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that passing an empty working copy will be as if the original compilation
	 * unit had been deleted.
	 * </p>
	 *
	 * @param workingCopies the working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing this type and all of its supertypes
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newSupertypeHierarchy(ISourceModule[] workingCopies, IProgressMonitor monitor)
		throws ModelException;
	
		
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes, considering types in the 
	 * working copies with the given owner. 
	 * In other words, the owner's working copies will take 
	 * precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that if a working copy is empty, it will be as if the original compilation
	 * unit had been deleted.
	 * <p>
	 *
	 * @param owner the owner of working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing this type and all of its supertypes
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner, IProgressMonitor monitor)
		throws ModelException;

	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project.
	 *
	 * @param project the given project
	 * @param monitor the given progress monitor
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project
	 */
	ITypeHierarchy newTypeHierarchy(IScriptProject project, IProgressMonitor monitor) throws ModelException;
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project, considering types in the 
	 * working copies with the given owner. 
	 * In other words, the owner's working copies will take 
	 * precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that if a working copy is empty, it will be as if the original compilation
	 * unit had been deleted.
	 * <p>
	 *
	 * @param project the given project
	 * @param owner the owner of working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project
	 */
	ITypeHierarchy newTypeHierarchy(IScriptProject project, WorkingCopyOwner owner, IProgressMonitor monitor) throws ModelException;

	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace.
	 *
	 * @param monitor the given progress monitor
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace
	 */
	ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws ModelException;
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace, 
	 * considering types in the given working copies. In other words, the list of working 
	 * copies that will take precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that passing an empty working copy will be as if the original compilation
	 * unit had been deleted.
	 *
	 * @param workingCopies the working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newTypeHierarchy(ISourceModule[] workingCopies, IProgressMonitor monitor) throws ModelException;
	
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace, 
	 * considering types in the working copies with the given owner. 
	 * In other words, the owner's working copies will take 
	 * precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that if a working copy is empty, it will be as if the original compilation
	 * unit had been deleted.
	 * <p>
	 *
	 * @param owner the owner of working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace
	 * @exception ModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner, IProgressMonitor monitor) throws ModelException;
}