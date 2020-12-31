package net.accela.prisma.ansi.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class Sauce {
    public final @NotNull String version;
    public final @NotNull String title;
    public final @NotNull String author;
    public final @NotNull String group;
    public final @NotNull String date;
    public final @Range(from = 0, to = 255) int dataType;
    public final @Range(from = 0, to = 255) int fileType;
    public final @Range(from = 0, to = 255) int typeInfo1;
    public final @Range(from = 0, to = 255) int typeInfo2;
    public final @Range(from = 0, to = 255) int typeInfo3;
    public final @Range(from = 0, to = 255) int typeInfo4;
    public final @Range(from = 0, to = 255) int typeFlags;
    public final @NotNull String typeInfoString;
    public final @Nullable String comments;

    public Sauce(final @NotNull String version,
                 final @NotNull String title,
                 final @NotNull String author,
                 final @NotNull String group,
                 final @NotNull String date,
                 final @Range(from = 0, to = 255) int dataType,
                 final @Range(from = 0, to = 255) int fileType,
                 final @Range(from = 0, to = 255) int typeInfo1,
                 final @Range(from = 0, to = 255) int typeInfo2,
                 final @Range(from = 0, to = 255) int typeInfo3,
                 final @Range(from = 0, to = 255) int typeInfo4,
                 final @Range(from = 0, to = 255) int typeFlags,
                 final @NotNull String typeInfoString,
                 final @Nullable String comments
    ) {
        this.version = version;
        this.title = title;
        this.author = author;
        this.group = group;
        this.date = date;
        this.dataType = dataType;
        this.fileType = fileType;
        this.typeInfo1 = typeInfo1;
        this.typeInfo2 = typeInfo2;
        this.typeInfo3 = typeInfo3;
        this.typeInfo4 = typeInfo4;
        this.typeFlags = typeFlags;
        this.typeInfoString = typeInfoString;
        this.comments = comments;

        // Validate values
        /*
        validateExactStringLength(version, "Version", 2);
        validateExactStringLength(title, "Title", 35);
        validateExactStringLength(author, "Author", 20);
        validateExactStringLength(group, "Group", 20);
        validateExactStringLength(date, "Date", 8);
         */

        validateintAsByte(dataType, "DataType", 1);
        validateintAsByte(fileType, "FileType", 1);

        validateintAsByte(typeInfo1, "TypeInfo1", 2);
        validateintAsByte(typeInfo2, "TypeInfo2", 2);
        validateintAsByte(typeInfo3, "TypeInfo3", 2);
        validateintAsByte(typeInfo4, "TypeInfo4", 2);

        validateintAsByte(typeFlags, "TypeFlags", 1);

        if (typeInfoString.length() > 22) throw new InvalidSauceException("TypeInfoString parameter length < 22");
        if (comments != null && comments.length() / 64 > 255) throw new InvalidSauceException("More than 255 comments");
    }

    private static void validateExactStringLength(@NotNull String parameter, @NotNull String parameterName, int length) {
        if (parameter.length() != length) {
            throw new InvalidSauceException(parameterName + " parameter length != " + length);
        }
    }

    private static void validateintAsByte(int parameter, @NotNull String parameterName, int byteCount) {
        final int boundsStart = 0, boundsEnd = 255 * byteCount;
        if (!(parameter >= boundsStart && parameter <= boundsEnd)) {
            throw new InvalidSauceException(String.format(
                    "%s (%d) parameter out of bounds (%d - %d)",
                    parameterName, parameter, boundsStart, boundsEnd
            ));
        }
    }

    public @NotNull String getVersion() {
        return version;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @NotNull String getAuthor() {
        return author;
    }

    public @NotNull String getGroup() {
        return group;
    }

    public @NotNull String getDate() {
        return date;
    }

    public @Range(from = 0, to = 255) int getDataType() {
        return dataType;
    }

    public @Range(from = 0, to = 255) int getFileType() {
        return fileType;
    }

    public @Range(from = 0, to = 255) int getTypeInfo1() {
        return typeInfo1;
    }

    public @Range(from = 0, to = 255) int getTypeInfo2() {
        return typeInfo1;
    }

    public @Range(from = 0, to = 255) int getTypeInfo3() {
        return typeInfo1;
    }

    public @Range(from = 0, to = 255) int getTypeInfo4() {
        return typeInfo1;
    }
}
