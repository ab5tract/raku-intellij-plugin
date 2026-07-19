package org.raku.comma

import org.raku.comma.inspection.inspections.AssignmentToImmutableInspection
import org.raku.comma.inspection.inspections.BuiltinSubmethodInspection
import org.raku.comma.inspection.inspections.CallArityInspection
import org.raku.comma.inspection.inspections.DeprecatedMethodInspection
import org.raku.comma.inspection.inspections.DuplicateConditionInspection
import org.raku.comma.inspection.inspections.GrepFirstInspection
import org.raku.comma.inspection.inspections.HashOrBlockInspection
import org.raku.comma.inspection.inspections.HyphenInCharacterClassInspection
import org.raku.comma.inspection.inspections.IdiomaticLoopInspection
import org.raku.comma.inspection.inspections.IllegalVariableDeclarationInspection
import org.raku.comma.inspection.inspections.InterpolatedEVALInspection
import org.raku.comma.inspection.inspections.LeadingZeroInspection
import org.raku.comma.inspection.inspections.ListAssignmentInspection
import org.raku.comma.inspection.inspections.MethodCallOnRangeInspection
import org.raku.comma.inspection.inspections.MissingRoleMethodInspection
import org.raku.comma.inspection.inspections.MissingThingsInspection
import org.raku.comma.inspection.inspections.MonitorUsageInspection
import org.raku.comma.inspection.inspections.MyScopedVariableExportedInspection
import org.raku.comma.inspection.inspections.NamedPairArgumentInspection
import org.raku.comma.inspection.inspections.NoEndpointRangeInspection
import org.raku.comma.inspection.inspections.NonInheritableComposableDeclarationInspection
import org.raku.comma.inspection.inspections.NonNilReturnInspection
import org.raku.comma.inspection.inspections.NoncomposableDoesInspection
import org.raku.comma.inspection.inspections.NotProgressingRegexInspection
import org.raku.comma.inspection.inspections.NullRegexInspection
import org.raku.comma.inspection.inspections.PodFormatterInspection
import org.raku.comma.inspection.inspections.ProblematicReturnInspection
import org.raku.comma.inspection.inspections.RakuExecutableStringInspection
import org.raku.comma.inspection.inspections.RakudoImplementationDetailInspection
import org.raku.comma.inspection.inspections.RedundantInitializationInspection
import org.raku.comma.inspection.inspections.SelfAvailabilityInspection
import org.raku.comma.inspection.inspections.SignatureInspection
import org.raku.comma.inspection.inspections.SimplifiedRangeInspection
import org.raku.comma.inspection.inspections.UndeclaredAttributeInspection
import org.raku.comma.inspection.inspections.UndeclaredOrDeprecatedRoutineInspection
import org.raku.comma.inspection.inspections.UndeclaredPrivateMethodInspection
import org.raku.comma.inspection.inspections.UndeclaredVariableInspection
import org.raku.comma.inspection.inspections.UnitKeywordInspection
import org.raku.comma.inspection.inspections.UnitSubInspection
import org.raku.comma.inspection.inspections.UnknownRegexModInspection
import org.raku.comma.inspection.inspections.UnusedRoutineInspection
import org.raku.comma.inspection.inspections.UnusedVariableInspection
import org.raku.comma.inspection.inspections.UsedModuleInspection
import org.raku.comma.inspection.inspections.UselessMethodDeclarationInspection
import org.raku.comma.inspection.inspections.UselessUseInspection
import org.raku.comma.inspection.inspections.WheneverOutsideOfReactInspection
import org.raku.comma.inspection.inspections.WithConstructionInspection
import org.raku.comma.inspection.inspections.ZeroArgSubInspection

// Every inspection the plugin registers; the legacy annotation/intention tests
// predate the annotator-to-inspection conversion and expect all of them active.
val ALL_RAKU_INSPECTIONS = arrayOf(
        AssignmentToImmutableInspection::class.java,
        BuiltinSubmethodInspection::class.java,
        CallArityInspection::class.java,
        DeprecatedMethodInspection::class.java,
        DuplicateConditionInspection::class.java,
        GrepFirstInspection::class.java,
        HashOrBlockInspection::class.java,
        HyphenInCharacterClassInspection::class.java,
        IdiomaticLoopInspection::class.java,
        IllegalVariableDeclarationInspection::class.java,
        InterpolatedEVALInspection::class.java,
        LeadingZeroInspection::class.java,
        ListAssignmentInspection::class.java,
        MethodCallOnRangeInspection::class.java,
        MissingRoleMethodInspection::class.java,
        MissingThingsInspection::class.java,
        MonitorUsageInspection::class.java,
        MyScopedVariableExportedInspection::class.java,
        NamedPairArgumentInspection::class.java,
        NoEndpointRangeInspection::class.java,
        NonInheritableComposableDeclarationInspection::class.java,
        NonNilReturnInspection::class.java,
        NoncomposableDoesInspection::class.java,
        NotProgressingRegexInspection::class.java,
        NullRegexInspection::class.java,
        PodFormatterInspection::class.java,
        ProblematicReturnInspection::class.java,
        RakuExecutableStringInspection::class.java,
        RakudoImplementationDetailInspection::class.java,
        RedundantInitializationInspection::class.java,
        SelfAvailabilityInspection::class.java,
        SignatureInspection::class.java,
        SimplifiedRangeInspection::class.java,
        UndeclaredAttributeInspection::class.java,
        UndeclaredOrDeprecatedRoutineInspection::class.java,
        UndeclaredPrivateMethodInspection::class.java,
        UndeclaredVariableInspection::class.java,
        UnitKeywordInspection::class.java,
        UnitSubInspection::class.java,
        UnknownRegexModInspection::class.java,
        UnusedRoutineInspection::class.java,
        UnusedVariableInspection::class.java,
        UsedModuleInspection::class.java,
        UselessMethodDeclarationInspection::class.java,
        UselessUseInspection::class.java,
        WheneverOutsideOfReactInspection::class.java,
        WithConstructionInspection::class.java,
        ZeroArgSubInspection::class.java
)
