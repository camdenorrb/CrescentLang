@0x87012c0ea71d2bcd; # Version 1.0

using Java = import "java.capnp";
$Java.package("me.camdenorrb.crescentvm.bytecode");
$Java.outerClassname("BytecodeFileFormat");

enum OpCodes @0x9a5c55536a1b926e{
	if        @0;
	else      @1;
	when      @2;
	while     @3;
	value     @4;
	type      @5;
	return    @6;
	fun       @7;
	var       @8;
	val       @9;
	add       @10;
	sub       @11;
	mul       @12;
	div       @13;
	assignEquals @14;
	verifyEquals @15;
	greaterThan  @16;
	lesserThan   @17;
	structure @18;
	endBlock  @19;
}

struct Map @0xca6e1f78455b473d {
  entries @0 :List(Entry);
  struct Entry @0xc161164e98ed7c26 {
    key @0 :DataType;
    value @1 :DataType;
  }
}

struct MemberTarget @0xfac093a791e80f14 {
	structureName @0 :Text;
	memberName @1 :Text;
}

struct DataType @0xc1723d934d27537d {
	union {
		void @0  :Void;
		i8   @1  :Int8;
		u8   @2  :UInt8;
		i16  @3  :Int16;
		u16  @4  :UInt16;
		i32  @5  :Int32;
		u32  @6  :UInt32;
		i64  @7  :Int64;
		u64  @8  :UInt64;
		f32  @9  :Float32;
		f64  @10 :Float64;
		char @11 :UInt8;
		array @12 :List(DataType);
		map   @13 :Map;
		text  @14 :Text;
		fun   @15 :MemberTarget;
	}
}

struct Operation @0xe1ee1ef5a6388e33 {
	
}

struct Member @0xa5ac835bd15b48b6 {
	name    @0 :Text;
	returnType @1 :DataType;
	
}

struct Structure @0x835d3fe2f08a143e {
	name    @0 :Text;
	members @1 :List(Member);
	importedStructures @2 :List(Text);
}

struct MoonPackage @0xc99e95e9f27b2ef4 {
	name    @0 :Text;
	structures @1 :List(Structure);
	union {
		entryPoint @2 :MemberTarget;
		noEntry @3 :Void;
	}
	externalDeps @4 :List(Text);
}