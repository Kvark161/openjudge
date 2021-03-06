import {ValidationResult} from "../shared/validation-response";

export class Utils {

    public static validateNotNull(value: any, fieldName: string, vr: ValidationResult) {
        if (value == undefined || value == null) {
            vr.addError(fieldName, "Can't be empty");
        }
    }

    public static validateNotEmptyAndNotLarger(value: string, fieldName: string, maxLength: number, vr: ValidationResult) {
        if (!value || value == "") {
            vr.addError(fieldName, "Can't be empty");
        } else if (value.length > maxLength) {
            vr.addError(fieldName, "Can't be larger than " + maxLength + " symbols");
        }
    }

    public static validateEmptyOrNotLarger(value: string, fieldName: string, maxLength: number, vr: ValidationResult) {
        if (value && value.length > maxLength) {
            vr.addError("interpreterPath", "Can't be larger than " + maxLength + " symbols");
        }
    }

    public static getMemoryInOptimalUnits(bytes: number) {
        let ans = {count: 0, units: ""};
        if (bytes < 1024) {
            ans = {count: bytes, units: "b"};
        } else if (bytes < 1024*1024) {
            ans = {count: bytes / 1024, units: "Kb"};
        } else {
            ans = {count: bytes / 1024 /1024, units: "Mb"};
        }
        return ans;
    }
}
