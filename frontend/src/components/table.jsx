// Correct export of Table component
import React from "react";
import { useTable } from "react-table";

const Table = ({ data, columns }) => {
  const {
    getTableProps,
    getTableBodyProps,
    headerGroups,
    rows,
    prepareRow,
  } = useTable({
    columns,
    data,
  });

  return (
    <div className="overflow-x-auto bg-[#1C2433] rounded-xl shadow-lg">
      <table {...getTableProps()} className="min-w-full text-[#E2E8F0]">
        <thead className="text-sm font-medium text-gray-300 bg-[#2A3548]">
          {headerGroups.map((headerGroup) => (
            <tr {...headerGroup.getHeaderGroupProps()}>
              {headerGroup.headers.map((column) => (
                <th
                  {...column.getHeaderProps()}
                  className="px-4 py-3 text-left border-b border-gray-700"
                >
                  {column.render("Header")}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody {...getTableBodyProps()} className="text-sm">
          {rows.map((row) => {
            prepareRow(row);
            return (
              <tr
                {...row.getRowProps()}
                className="bg-[#2A3548] hover:bg-[#3A4754]"
              >
                {row.cells.map((cell) => (
                  <td
                    {...cell.getCellProps()}
                    className="px-4 py-3 text-left border-b border-gray-700"
                  >
                    {cell.render("Cell")}
                  </td>
                ))}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default Table;  // Make sure this is the default export
